#!/bin/bash
set -e
# Prompt for environment
read -p "Enter the environment (e.g., dev): " env
env_path="./$env"

# Check if the environment directory exists
if [[ ! -d $env_path ]]; then
    echo "The specified environment '$env' does not exist. Exiting."
    exit 1
else
    echo "Environment '$env' found. Navigating to $env_path..."
    cd "$env_path"
fi

# Function to install gcloud CLI
install_gcloud() {
    echo "Installing gcloud CLI..."
    # Install gcloud for Debian/Ubuntu; customize as needed for other OSes
    curl -sSL https://sdk.cloud.google.com | bash
    exec -l $SHELL
    echo "gcloud installed successfully."
}

# Check if gcloud CLI is installed
if ! command -v gcloud &> /dev/null; then
    echo "gcloud CLI is not installed."
    read -p "Would you like to install gcloud CLI? (y/n): " install_gcloud_answer
    if [[ $install_gcloud_answer == "y" ]]; then
        install_gcloud
    else
        echo "gcloud CLI is required. Exiting."
        exit 1
    fi
else
    echo "gcloud CLI is already installed."
fi

# Check if gcloud is already authenticated
if gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q '@'; then
    echo "gcloud is already authenticated. Skipping 'gcloud init'."
else
    # Initialize gcloud if not already authenticated
    echo "Initializing gcloud..."
    gcloud init
fi

# Check if application default credentials file exists
if [[ -f ~/.config/gcloud/application_default_credentials.json ]]; then
    echo "Application default credentials are already set. Skipping 'gcloud auth application-default login'."
else
    echo "Setting up application default credentials..."
    gcloud auth application-default login --quiet
fi

# Capture project ID from gcloud config
project_id=$(gcloud config list --format="value(core.project)")
if [ -z "$project_id" ]; then
    echo "Failed to retrieve the project ID from gcloud config. Exiting."
    exit 1
fi
echo "Using project ID from gcloud config: $project_id"

# Function to install Terraform
install_terraform() {
    echo "Installing Terraform..."
    # Install Terraform for Debian/Ubuntu; customize as needed for other OSes
    sudo apt-get update && sudo apt-get install -y gnupg software-properties-common curl
    curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
    echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
    sudo apt-get update && sudo apt-get install -y terraform
    echo "Terraform installed successfully."
}

# Check if Terraform is installed
if ! command -v terraform &> /dev/null; then
    echo "Terraform is not installed."
    read -p "Would you like to install Terraform? (y/n): " install_terraform_answer
    if [[ $install_terraform_answer == "y" ]]; then
        install_terraform
    else
        echo "Terraform is required. Exiting."
        exit 1
    fi
else
    echo "Terraform is already installed."
fi

# Generate SSH key pair in ./secrets
mkdir -p /tmp/secrets
if [[ ! -f /tmp/secrets/id_rsa ]]; then
    ssh-keygen -t rsa -f ./secrets/id_rsa -q -N ""
    echo "SSH key pair generated at ./secrets/id_rsa and ./secrets/id_rsa.pub."
else
    echo "SSH key pair already exists at ./secrets/id_rsa and ./secrets/id_rsa.pub."
fi

# Run Terraform apply with the project_id variable
echo "Running 'terraform apply --auto-approve' with project_id: $project_id..."
terraform init
terraform fmt
terraform validate
terraform apply --auto-approve -var="project_id=$project_id"
terraform output --json > terraform-output.json
chmod 744 ../scripts/to-ansible-inventory.sh
../scripts/to-ansible-inventory.sh

# Parse instances.ini to extract public IPs and show SSH instructions
if [[ -f instances.ini ]]; then
    echo "Parsing instances.ini to retrieve public IPs..."
    master_nodes=($(grep -A1 "\[master-nodes\]" instances.ini | tail -n +2))
    worker_nodes=($(grep -A1 "\[worker-nodes\]" instances.ini | tail -n +2))

    echo "SSH Instructions:"
    echo "To connect to a master node, use the following command:"
    echo "ssh -i ./secrets/id_rsa user@${master_nodes[0]}"
    
    echo "To connect to a worker node, use the following command:"
    echo "ssh -i ./secrets/id_rsa user@${worker_nodes[0]}"
else
    echo "instances.ini file not found. Please make sure it exists."
fi