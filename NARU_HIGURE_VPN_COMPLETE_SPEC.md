# NARU HIGURE VPN — complete project information

## App identity
- App name: NARU HIGURE VPN
- Android application ID: `com.naruhigure.vpn`
- Brand style: black background, neon purple accents, anime/cyberpunk artwork
- Main screen concept: branded header and character artwork; server selector; connection-method selector; large circular CONNECT button; truthful connection status; advanced settings; Home, Stats, Settings and About navigation.
- Do not show the instruction “tap the round button to change connection state”.
- Do not show a separate “VPN OFF” label.
- The app must only display CONNECTED after the Android VPN tunnel reports an active connection. While starting, show CONNECTING; on stop/failure, show DISCONNECTED or an error.

## Server/location choices
1. Random Server — Any Location
2. United States — New York
3. Canada — Montreal
4. Germany — Frankfurt
5. United Kingdom — London
6. Netherlands — Amsterdam
7. France — Paris
8. Austria — Vienna
9. Australia — Tasmania
10. Brazil — Sao Paulo
11. Singapore — Singapore
12. India — Bangalore
13. Game | EU

These are the requested choices, not provisioned servers. Each requires a real endpoint and a valid configuration from a server operator. Random Server can only choose among configured, reachable servers.

## Connection methods and settings
- SSH
- Slow DNS
- SSL/TLS
- WireGuard
- HTTP Proxy
- SNI Host
- Payload
- Custom DNS (default shown: `8.8.8.8`)
- SSH ports requested: 80, 443 and 8080, only when the authorised provider supports them.
- SNI Host and Payload are settings for compatible tunnel methods, not standalone protocols.
- DNS resolver address is not a VPN endpoint.

## Connection behavior
- Main action toggles connect/disconnect.
- Show CONNECTING while the tunnel is being established.
- Show CONNECTED only when the Android VPN backend confirms the tunnel is up.
- On disconnect, show DISCONNECTED.
- Do not fake a connected state or promise unlimited data.

## Current source-code status in this ZIP
- Includes Android source project and GitHub Actions workflow for a debug APK build.
- Includes a WireGuard tunnel backend. A valid WireGuard client configuration from a server you own or are authorised to use must be imported on the phone.
- Includes UI/configuration fields for Slow DNS server address and optional port, but **does not include the separate working Slow DNS engine from the user's other APK**.
- SSH, SSL/TLS and HTTP Proxy engines are not included. Their server-side/provider details are also not included.
- Country entries are labels only; no actual country servers have been provisioned.
- The design-reference image is a visual mockup, not an installed-app screenshot or functioning APK.

## Missing items that cannot be inferred or safely invented
- The source project/engine from the user's previously working Slow DNS APK.
- Actual authorised server hostnames/IP addresses, ports, credentials, keys, certificates, and provider-specific tunnel parameters.
- SSH/SSL/HTTP proxy tunnel engines.
- Server infrastructure and account credentials.

Do not put passwords, private keys, or personal VPN configuration files into a public GitHub repository. Keep secrets private and import them locally on the device. Only use servers and network access you are authorised to use.
