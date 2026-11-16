#!/bin/bash

printf 'ARGS: <%s> <%s> <%s>\n' "$1" "$2" "$3" >&2

if [ "$1" = "git" ] && [ "$2" = "set" ] && [ "$3" = "remote" ]; then
    echo "set-url"
fi