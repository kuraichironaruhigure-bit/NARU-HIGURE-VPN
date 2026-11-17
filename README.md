# NARU HIGURE VPN — Android source + GitHub APK builder

This repository contains the Android source project, the GitHub Actions APK workflow, and the complete set of VPN requirements known from the project discussions.

## Build APK on GitHub
1. Create a GitHub repository named `NARU-HIGURE-VPN`.
2. Extract this ZIP.
3. Upload the **contents** of `NaruHigureVPN_Android_Project` to the repository root, including `.github`.
4. Commit to the `main` branch.
5. Open **Actions** and run **Build NARU HIGURE VPN APK**.
6. Download the `NARU-HIGURE-VPN-debug` artifact from the successful workflow run, then extract it to get the APK.

## Project information
See `project-info/NARU_HIGURE_VPN_COMPLETE_SPEC.md`, `project-info/server-locations.json`, and `project-info/connection-options.json` for the full requested server list, connection options, design requirements, and implementation status.

## Important
This is source code, not a prebuilt APK. GitHub Actions compiles the project; it does not create VPN servers or supply missing tunnel engines. The project includes a WireGuard backend, but it requires an authorised WireGuard client config. Slow DNS settings are present, but the working Slow DNS engine from a separate APK is **not** included. SSH, SSL/TLS, and HTTP Proxy are listed as options but their tunnel engines are not included. The country names are not live server endpoints.

Do not upload private keys, passwords, or personal VPN configs to a public repository. Use only servers and network access you are authorised to use. See `project-info/NARU_HIGURE_VPN_COMPLETE_SPEC.md` for details.
