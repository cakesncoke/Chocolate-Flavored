#!/usr/bin/env bash
# Run inside xvfb-run. Verify startup plus a real cooking-pot menu in the saved verification world.
set -euo pipefail
mkdir -p build/client-smoke run/saves/CookingVerification
cp -a run-verification/world/. run/saves/CookingVerification/
printf 'onboardAccessibility:false\nguiScale:2\ntutorialStep:none\nrenderDistance:3\nsimulationDistance:5\nmaxFps:60\nenableVsync:false\n' > run/options.txt
bash ./gradlew runClient -PverifyCookingClient=true --no-daemon --console=plain > build/client-smoke/console.log 2>&1 &
client_pid=$!
trap 'kill "$client_pid" 2>/dev/null || true' EXIT

# Allow dependency/asset downloads and software rendering on a fresh CI runner.
for ((attempt=0; attempt<120; attempt++)); do
    if ! kill -0 "$client_pid" 2>/dev/null; then
        cat build/client-smoke/console.log
        echo 'Client exited before startup verification.' >&2
        exit 1
    fi
    # Leave eight seconds between galleries so the five-second polling loop captures both.
    if [[ -f run/logs/latest.log && ! -f build/client-smoke/animals.png ]] &&
       grep -q ANIMAL_CLIENT_VERIFICATION_PASSED run/logs/latest.log; then
        import -window root build/client-smoke/animals.png
    fi
    if [[ -f run/logs/latest.log ]] &&
       grep -q 'Initializing Chocolate Flavored' run/logs/latest.log &&
       grep -q 'Created: .*textures/atlas/' run/logs/latest.log &&
       grep -q COOKING_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&
       grep -q ANIMAL_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&
       grep -q MOB_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&
       grep -q TUFF_GOLEM_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&
       grep -q SUNKEN_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&
       grep -q CROP_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&
       grep -q CAKE_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q DYED_WOOD_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q STEEL_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q TRIM_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q TIDE_JUMP_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q TRIM_OUTLINE_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q DUNE_OUTLINE_CLIENT_VERIFICATION_PASSED run/logs/latest.log &&\
       grep -q REFINEMENT_CLIENT_VERIFICATION_PASSED run/logs/latest.log; then
        # Keep the client alive past resource loading, and capture the display.
        sleep 20
        kill -0 "$client_pid"
        if [[ -d run/crash-reports ]] && find run/crash-reports -name '*.txt' -print -quit | grep -q .; then
            cat build/client-smoke/console.log
            exit 1
        fi
        import -window root build/client-smoke/client.png
        cp run/logs/latest.log build/client-smoke/latest.log
        echo 'Client remained running after mod initialization, texture atlas loading and cooking-pot screen and adult/baby animal renderer verification.'
        exit 0
    fi
    sleep 5
done
cat build/client-smoke/console.log
import -window root build/client-smoke/failure.png || true
echo 'Timed out waiting for client initialization.' >&2
exit 1
