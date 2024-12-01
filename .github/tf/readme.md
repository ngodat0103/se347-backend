# Project Stucture
.
├── dev
│   ├── main.tf
│   ├── output.tf
│   ├── quick-start.sh
│   ├── secrets
│   │   ├── id_rsa
│   │   └── id_rsa.pub
│   ├── terraform.tfstate
│   ├── terraform.tfstate.backup
│   └── variables.tf
├── modules
│   ├── instances
│   │   ├── main.tf
│   │   ├── outputs.tf
│   │   └── variables.tf
│   ├── instances-group
│   │   ├── main.tf
│   │   ├── output.tf
│   │   └── variable.tf
│   └── master-nodes-internal-lb
│       ├── main.tf
│       ├── output.tf
│       └── variables.tf
├── readme.md
└── scripts
    ├── gcloud-logout.sh
    ├── get-project-id.sh
    ├── quick-start.sh
    └── to-ansible-inventory.sh

8 directories, 22 files
