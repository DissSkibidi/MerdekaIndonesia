
package id.todprojects.merdeka;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;

public final class MerdekaIndonesia extends JavaPlugin {
    private boolean running = false;
    private BukkitTask task;
    private int remaining;
    private int fireworkTicks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("Event Kemerdekaan Indonesia siap!");
    }

    @Override
    public void onDisable() {
        if (task != null) task.cancel();
    }

    @Override
    public boolean onCommand(
            CommandSender sender, Command command,
            String label, String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "mulai" -> {
                if (!sender.hasPermission("merdeka.admin")) {
                    sender.sendMessage(c("&cKhusus admin/OP."));
                    return true;
                }
                start(sender);
            }
            case "stop" -> {
                if (!sender.hasPermission("merdeka.admin")) {
                    sender.sendMessage(c("&cKhusus admin/OP."));
                    return true;
                }
                stop(true);
            }
            case "hadiah" -> reward(sender);
            default -> help(sender);
        }
        return true;
    }

    private void start(CommandSender sender) {
        if (running) {
            sender.sendMessage(c("&eEvent sudah berjalan."));
            return;
        }

        running = true;
        remaining = Math.max(
                10, getConfig().getInt("event.duration-seconds", 120));
        fireworkTicks = 0;

        Bukkit.broadcastMessage(c("&c&l================================"));
        Bukkit.broadcastMessage(c("&f&l HARI KEMERDEKAAN INDONESIA"));
        Bukkit.broadcastMessage(c("&c&l           17 AGUSTUS"));
        Bukkit.broadcastMessage(c("&fMerdeka! Merdeka! Merdeka!"));
        Bukkit.broadcastMessage(c("&eKetik &a/merdeka hadiah &euntuk hadiah."));
        Bukkit.broadcastMessage(c("&c&l================================"));

        int interval = Math.max(
                1, getConfig().getInt("event.firework-interval-seconds", 5));

        task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            remaining--;
            fireworkTicks++;

            if (fireworkTicks >= interval) {
                fireworks();
                fireworkTicks = 0;
            }

            if (remaining <= 0) stop(true);
        }, 20L, 20L);

        sender.sendMessage(c("&aEvent berhasil dimulai!"));
    }

    private void stop(boolean announce) {
        running = false;

        if (task != null) {
            task.cancel();
            task = null;
        }

        if (announce) {
            Bukkit.broadcastMessage(
                    c("&cEvent Kemerdekaan telah selesai! &fTerima kasih."));
        }
    }

    private void fireworks() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = p.getLocation().add(0, 1.5, 0);
            Firework fw = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();

            meta.setPower(1);
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.RED, Color.WHITE)
                    .withFade(Color.WHITE, Color.RED)
                    .trail(true)
                    .flicker(true)
                    .build());

            fw.setFireworkMeta(meta);
        }
    }

    private void reward(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Perintah ini hanya untuk pemain.");
            return;
        }

        Material mat = Material.matchMaterial(
                getConfig().getString(
                        "event.reward.material", "FIREWORK_ROCKET"));

        if (mat == null || !mat.isItem()) {
            mat = Material.FIREWORK_ROCKET;
        }

        int amount = Math.max(1, Math.min(
                64, getConfig().getInt("event.reward.amount", 8)));

        ItemStack item = new ItemStack(mat, amount);

        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(c("&cInventory penuh!"));
            return;
        }

        p.getInventory().addItem(item);
        p.sendMessage(c("&aHadiah kemerdekaan: &f"
                + amount + "x " + mat.name()));
    }

    private void help(CommandSender sender) {
        sender.sendMessage(c("&c&l=== MERDEKA INDONESIA ==="));
        sender.sendMessage(c("&f/merdeka info &7- Bantuan"));
        sender.sendMessage(c("&f/merdeka hadiah &7- Ambil hadiah"));

        if (sender.hasPermission("merdeka.admin")) {
            sender.sendMessage(c("&f/merdeka mulai &7- Mulai event"));
            sender.sendMessage(c("&f/merdeka stop &7- Stop event"));
        }
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
                    }
              
