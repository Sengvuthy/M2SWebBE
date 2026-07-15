#!/bin/bash
echo ">>> Creating uploads/products directory..."
mkdir -p /var/app/current/uploads/products
chown -R webapp:webapp /var/app/current/uploads/products
chmod -R 755 /var/app/current/uploads/products
echo ">>> Uploads directory ready."
