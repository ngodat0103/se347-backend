#!/bin/bash
export project_id=$(gcloud config list --format="value(core.project)")
if [ -z "$project_id" ]; then
    echo "Failed to retrieve the project ID from gcloud config. Exiting."
    exit 1
fi