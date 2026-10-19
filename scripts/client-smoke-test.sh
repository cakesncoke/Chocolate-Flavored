#!/usr/bin/env bash
# Run inside xvfb-run. This verifies startup, not world/gameplay behavior.
set -euo pipefail
mkdir -p build/client-smoke
bash ./gradlew runClient --no-daemon --console=plain > build/client-smoke/console.log 2>&1 &
client_pid=$!
trap 'kill "$client_pid" 2>/dev/null || true' EXIT

# Allow dependency/asset downloads and software rendering on a fresh CI runner.
for ((attempt=0; attempt<120; attempt++)); do
    if ! kill -0 "$client_pid" 2>/dev/null; then
        cat build/client-smoke/console.log
        echo 'Client exited before startup verification.' >&2
        exit 1
    fi
    if [[ -f run/logs/latest.log ]] &&
       grep -q 'Initializing Chocolate Flavored' run/logs/latest.log &&
       grep -q 'Created: .*textures/atlas/' run/logs/latest.log; then
        # Keep the client alive past resource loading, and capture the display.
        sleep 20
        kill -0 "$client_pid"
        if [[ -d run/crash-reports ]] && find run/crash-reports -name '*.txt' -print -quit | grep -q .; then
            cat build/client-smoke/console.log
            exit 1
        fi
        import -window root build/client-smoke/client.png
        cp run/logs/latest.log build/client-smoke/latest.log
        echo 'Client remained running after mod initialization and texture atlas loading.'
        exit 0
    fi
    sleep 5
done
cat build/client-smoke/console.log
echo 'Timed out waiting for client initialization.' >&2
exit 1
