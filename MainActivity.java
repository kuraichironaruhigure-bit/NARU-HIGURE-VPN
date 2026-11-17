package com.naruhigure.vpn;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.android.backend.Tunnel.State;
import com.wireguard.config.Config;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int PICK_CONFIG = 1001, VPN_PERMISSION = 1002;
    private static final String CONFIG_FILE = "naru-wireguard.conf";
    private Backend backend;
    private TextView status, configStatus, serverValue, protocolValue;
    private String selectedServer = "Random Server — Any Location";
    private String selectedProtocol = "WireGuard";
    private String slowDnsHost = "";
    private String slowDnsPort = "";
    private Button connectButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Tunnel tunnel = new Tunnel() {
        @Override public String getName() { return "NaruHigure"; }
        @Override public void onStateChange(State state) {
            runOnUiThread(() -> setStatus(state == State.UP ? "Connected" : "Disconnected"));
        }
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        backend = new GoBackend(this);
        android.content.SharedPreferences prefs = getSharedPreferences("naru_settings", MODE_PRIVATE);
        slowDnsHost = prefs.getString("slow_dns_host", "");
        slowDnsPort = prefs.getString("slow_dns_port", "");
        buildUi();
        refreshConfigStatus();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(12), dp(14), dp(18));
        page.setBackgroundColor(Color.rgb(0,0,0));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView menu = text("☰", 28, Color.WHITE, false);
        top.addView(menu, new LinearLayout.LayoutParams(dp(42), dp(58)));
        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("NARU HIGURE VPN", 22, Color.WHITE, true);
        TextView subtitle = text("Fast • Secure • Private", 14, 0xFFBBBBBB, false);
        titleBox.addView(title); titleBox.addView(subtitle);
        top.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text("⋮", 28, Color.WHITE, false));
        page.addView(top);

        ImageView banner = new ImageView(this);
        banner.setImageResource(R.drawable.naru_banner);
        banner.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(174));
        bp.topMargin = dp(10); bp.bottomMargin = dp(12);
        banner.setLayoutParams(bp);
        page.addView(banner);

        LinearLayout connectCard = card();
        TextView small = text("CONNECTION STATUS", 13, 0xFFBBBBBB, true);
        small.setGravity(Gravity.CENTER);
        connectCard.addView(small);
        LinearLayout metrics = new LinearLayout(this);
        metrics.setGravity(Gravity.CENTER);
        TextView up = text("▥\n0 B/s\nUpload", 15, Color.WHITE, true);
        up.setGravity(Gravity.CENTER);
        TextView down = text("⇩\n0 B/s\nDownload", 15, Color.WHITE, true);
        down.setGravity(Gravity.CENTER);
        metrics.addView(up, new LinearLayout.LayoutParams(0, dp(128), 1));
        connectButton = new Button(this);
        connectButton.setText("⬡\nCONNECT");
        connectButton.setTextSize(18);
        connectButton.setTextColor(Color.WHITE);
        connectButton.setAllCaps(false);
        connectButton.setBackground(round(0xFF101010, 0xFFEAEAEA, 100));
        metrics.addView(connectButton, new LinearLayout.LayoutParams(0, dp(128), 1.7f));
        metrics.addView(down, new LinearLayout.LayoutParams(0, dp(128), 1));
        connectCard.addView(metrics);
        status = text("●  Disconnected", 17, Color.WHITE, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(8), dp(12), dp(8), dp(12));
        status.setBackground(round(0xFF090909, 0xFF444444, 14));
        connectCard.addView(status);
        page.addView(connectCard);

        LinearLayout serverCard = card();
        TextView sh = text("☷  Server Configuration                 ⚙", 20, Color.WHITE, true);
        serverCard.addView(sh);
        serverValue = text("◎  Server:                 Not configured     ›", 16, Color.WHITE, false);
        serverCard.addView(serverValue);
        protocolValue = text("⬡  Protocol:                " + selectedProtocol + "          ›", 16, Color.WHITE, false);
        protocolValue.setOnClickListener(v -> showProtocolPicker());
        serverCard.addView(protocolValue);
        TextView slowDnsValue = text("Slow DNS settings:  " + (slowDnsHost.isEmpty() ? "Not configured" : slowDnsHost + ":" + slowDnsPort), 14, 0xFFBBBBBB, false);
        slowDnsValue.setOnClickListener(v -> showSlowDnsSettings(slowDnsValue));
        serverCard.addView(slowDnsValue);
        serverCard.addView(text("DNS resolver:                 8.8.8.8             ›", 16, Color.WHITE, false));
        configStatus = text("Configuration: Not imported", 13, 0xFFBBBBBB, false);
        serverCard.addView(configStatus);
        page.addView(serverCard);

        LinearLayout row1 = new LinearLayout(this);
        Button servers = actionButton("☷  Server List");
        servers.setOnClickListener(v -> Toast.makeText(this, "Import a server configuration to add a server.", Toast.LENGTH_LONG).show());
        Button import = actionButton("⇩  Import Config");
        import.setOnClickListener(v -> chooseConfig());
        row1.addView(servers, weight()); row1.addView(import, weight());
        page.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        Button settings = actionButton("⚙  Settings");
        settings.setOnClickListener(v -> Toast.makeText(this, "DNS: 8.8.8.8 • Protocol: WireGuard", Toast.LENGTH_LONG).show());
        Button about = actionButton("ⓘ  About");
        about.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
                .setTitle("NARU HIGURE VPN").setMessage("WireGuard VPN client starter. A real VPN server and valid configuration are required.")
                .setPositiveButton("OK", null).show());
        row2.addView(settings, weight()); row2.addView(about, weight());
        page.addView(row2);

        TextView footer = text("──────────   Naru Higure VPN v1.0   ──────────\nStay Connected, Stay Secure", 13, 0xFFCCCCCC, false);
        footer.setGravity(Gravity.CENTER); footer.setPadding(0, dp(20), 0, dp(8));
        page.addView(footer);

        connectButton.setOnClickListener(v -> requestConnect());
        scroll.addView(page);
        setContentView(scroll);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackground(round(0xFF151515, 0xFF454545, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(12); c.setLayoutParams(p);
        return c;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(dp(5), dp(7), dp(5), dp(7)); return t;
    }
    private Button actionButton(String s) {
        Button b = new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setBackground(round(0xFF171717, 0xFF555555, 14)); return b;
    }
    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(62), 1);
        p.setMargins(dp(3), dp(3), dp(3), dp(7)); return p;
    }
    private GradientDrawable round(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke); return d;
    }
    private int dp(int x) { return (int)(x * getResources().getDisplayMetrics().density + .5f); }

    
    private void showServerPicker() {
        final String[] locations = {
            "Random Server — Any Location",
            "United States — New York",
            "Canada — Montreal",
            "Germany — Frankfurt",
            "United Kingdom — London",
            "Netherlands — Amsterdam",
            "France — Paris",
            "Austria — Vienna",
            "Australia — Tasmania",
            "Brazil — Sao Paulo",
            "Singapore — Singapore",
            "India — Bangalore",
            "Game | EU"
        };
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select VPN location")
            .setItems(locations, (dialog, which) -> {
                selectedServer = locations[which];
                serverValue.setText("◎  Server:  " + selectedServer + "     ›");
                Toast.makeText(this,
                    "Selected " + selectedServer + ". A compatible server configuration is still required.",
                    Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showProtocolPicker() {
        final String[] protocols = {"SSH", "Slow DNS", "SSL/TLS", "WireGuard", "HTTP Proxy", "SNI Host", "Payload", "Custom DNS"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select connection method")
            .setSingleChoiceItems(protocols, java.util.Arrays.asList(protocols).indexOf(selectedProtocol), (dialog, which) -> {
                selectedProtocol = protocols[which];
                protocolValue.setText("⬡  Protocol:                " + selectedProtocol + "          ›");
                dialog.dismiss();
                if ("Slow DNS".equals(selectedProtocol)) showSlowDnsSettings(null);
            })
            .setNegativeButton("Cancel", null).show();
    }

    private void showSlowDnsSettings(TextView summary) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), dp(4));
        EditText host = new EditText(this);
        host.setSingleLine(true);
        host.setHint("Slow DNS server address");
        host.setText(slowDnsHost);
        EditText port = new EditText(this);
        port.setSingleLine(true);
        port.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        port.setHint("Server port (if supplied)");
        port.setText(slowDnsPort);
        form.addView(host); form.addView(port);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Slow DNS settings")
            .setMessage("Enter the server details supplied by your authorised Slow DNS provider. This saves the details only; a compatible Slow DNS tunnel engine is not included yet.")
            .setView(form)
            .setPositiveButton("Save", (d, w) -> {
                slowDnsHost = host.getText().toString().trim();
                slowDnsPort = port.getText().toString().trim();
                getSharedPreferences("naru_settings", MODE_PRIVATE).edit()
                    .putString("slow_dns_host", slowDnsHost).putString("slow_dns_port", slowDnsPort).apply();
                if (summary != null) summary.setText("Slow DNS settings:  " + (slowDnsHost.isEmpty() ? "Not configured" : slowDnsHost + (slowDnsPort.isEmpty() ? "" : ":" + slowDnsPort)));
                Toast.makeText(this, "Slow DNS details saved on this device.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null).show();
    }

    private void requestConnect() {
        if ("Slow DNS".equals(selectedProtocol)) {
            Toast.makeText(this, "Slow DNS settings are saved, but this build does not include a Slow DNS tunnel engine. It cannot connect using Slow DNS yet.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!"WireGuard".equals(selectedProtocol)) {
            Toast.makeText(this, selectedProtocol + " is a selectable option only. Its connection engine is not included in this build.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!new File(getFilesDir(), CONFIG_FILE).exists()) {
            Toast.makeText(this, "Import a valid WireGuard .conf file first. No server is configured yet.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = VpnService.prepare(this);
        if (i != null) startActivityForResult(i, VPN_PERMISSION); else connectWithConfig();
    }
    private void chooseConfig() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*"); startActivityForResult(i, PICK_CONFIG);
    }
    @Override protected void onActivityResult(int r, int result, Intent data) {
        super.onActivityResult(r, result, data);
        if (r == PICK_CONFIG && result == RESULT_OK && data != null && data.getData() != null) {
            try (InputStream in = getContentResolver().openInputStream(data.getData());
                 FileOutputStream out = new FileOutputStream(new File(getFilesDir(), CONFIG_FILE))) {
                if (in == null) throw new IOException("Unable to read file");
                byte[] buf = new byte[4096]; int n; while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                refreshConfigStatus(); Toast.makeText(this, "Configuration imported.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
        } else if (r == VPN_PERMISSION && result == RESULT_OK) connectWithConfig();
    }
    private void connectWithConfig() {
        executor.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(getFilesDir(), CONFIG_FILE)), StandardCharsets.UTF_8))) {
                Config config = Config.parse(reader); backend.setState(tunnel, State.UP, config);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
    private void setStatus(String s) { status.setText("●  " + s); }
    private void refreshConfigStatus() {
        boolean has = new File(getFilesDir(), CONFIG_FILE).exists();
        configStatus.setText(has ? "Configuration: Imported" : "Configuration: Not imported");
        serverValue.setText("◎  Server:  " + selectedServer + (has ? " (config imported)" : " (no endpoint configured)") + "     ›");
    }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
