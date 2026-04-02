#!/bin/bash
set -e
cd "$(dirname "$0")"
docker build -t smartoffice-mail:latest .
echo "Build success: smartoffice-mail:latest"
