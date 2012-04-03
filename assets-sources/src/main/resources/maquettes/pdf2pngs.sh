#!/bin/sh
set -e
convert -density 300x300 "$1" -density 300x300 "$2"
