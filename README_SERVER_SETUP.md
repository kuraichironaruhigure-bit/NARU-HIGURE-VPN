# Server-side requirements (not yet provisioned)

No server has been created by this project. The app cannot connect until a real WireGuard server is provisioned and a matching client configuration is issued.

A server administrator needs to:
1. Provision an always-on Linux VPS with a public IPv4 address.
2. Install and configure WireGuard using the official documentation.
3. Enable IP forwarding and configure routing/NAT appropriate to the server's network.
4. Allow the chosen WireGuard UDP port in both the VPS firewall and provider firewall.
5. Generate separate server and client keys.
6. Add the client's public key as a peer on the server.
7. Generate a client `.conf` containing the client's private key, assigned tunnel address, DNS (for example `8.8.8.8`), server public key, server endpoint and allowed IPs.
8. Transfer the client config securely to the phone and import it in NARU HIGURE VPN.

Never place a server private key in the Android app. Never send private keys in chat.

Official documentation: https://www.wireguard.com/quickstart/
