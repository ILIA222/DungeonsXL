/*
 * Copyright (C) 2012-2016 Frank Baumann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.dre2n.dungeonsxl.sign;

import io.github.dre2n.commons.util.NumberUtil;
import io.github.dre2n.commons.util.messageutil.MessageUtil;
import io.github.dre2n.dungeonsxl.config.DMessages;
import io.github.dre2n.dungeonsxl.game.Game;
import io.github.dre2n.dungeonsxl.game.GameType;
import io.github.dre2n.dungeonsxl.game.GameTypeDefault;
import io.github.dre2n.dungeonsxl.player.DGamePlayer;
import io.github.dre2n.dungeonsxl.player.DGroup;
import io.github.dre2n.dungeonsxl.trigger.InteractTrigger;
import io.github.dre2n.dungeonsxl.util.ProgressBar;
import io.github.dre2n.dungeonsxl.world.GameWorld;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Frank Baumann, Milan Albrecht, Daniel Saukel
 */
public class ReadySign extends DSign {

    private DSignType type = DSignTypeDefault.READY;

    private GameType gameType;
    private double autoStart = -1;
    private boolean triggered = false;

    public ReadySign(Sign sign, String[] lines, GameWorld gameWorld) {
        super(sign, lines, gameWorld);
    }

    /**
     * @return the gameType
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * @param gameType
     * the gameType to set
     */
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    /**
     * @return the time until the game starts automatically; -1 for no auto start
     */
    public double getTimeToAutoStart() {
        return autoStart;
    }

    /**
     * @param time
     * the time in seconds until the game starts automatically; -1 for no auto start
     */
    public void setTimeToAutoStart(double time) {
        autoStart = time;
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void onInit() {
        if (plugin.getGameTypes().getBySign(this) != null) {
            gameType = plugin.getGameTypes().getBySign(this);

        } else {
            gameType = GameTypeDefault.DEFAULT;
        }

        if (!lines[2].isEmpty()) {
            autoStart = NumberUtil.parseDouble(lines[2], -1);
        }

        if (!getTriggers().isEmpty()) {
            getSign().getBlock().setType(Material.AIR);
            return;
        }

        InteractTrigger trigger = InteractTrigger.getOrCreate(0, getSign().getBlock(), getGameWorld());
        if (trigger != null) {
            trigger.addListener(this);
            addTrigger(trigger);
        }

        getSign().setLine(0, ChatColor.DARK_BLUE + "############");
        getSign().setLine(1, ChatColor.DARK_GREEN + "Ready");
        getSign().setLine(2, ChatColor.DARK_RED + gameType.getSignName());
        getSign().setLine(3, ChatColor.DARK_BLUE + "############");
        getSign().update();
    }

    @Override
    public boolean onPlayerTrigger(Player player) {
        ready(DGamePlayer.getByPlayer(player));

        if (!triggered && autoStart >= 0) {
            triggered = true;

            new BukkitRunnable() {
                @Override
                public void run() {
                    onTrigger();
                }
            }.runTaskLater(plugin, (long) (autoStart * 20));

            if (!DGroup.getByPlayer(player).isPlaying()) {
                ProgressBar.sendProgressBar(getGame().getPlayers(), (int) Math.ceil(autoStart));
            }
        }

        return true;
    }

    @Override
    public void onTrigger() {
        for (Player player : Game.getByGameWorld(getGameWorld()).getPlayers()) {
            ready(DGamePlayer.getByPlayer(player));
        }
    }

    private void ready(DGamePlayer dPlayer) {
        if (dPlayer == null) {
            return;
        }

        if (dPlayer.isReady()) {
            return;
        }

        if (getGameWorld().getSignClass().isEmpty() || dPlayer.getDClass() != null) {
            GameType forced = getGameWorld().getConfig().getForcedGameType();
            dPlayer.ready(forced == null ? gameType : forced);
        }

        if (dPlayer.isReady()) {
            MessageUtil.sendMessage(dPlayer.getPlayer(), plugin.getMessageConfig().getMessage(dPlayer.isReady() ? DMessages.PLAYER_READY : DMessages.ERROR_READY));
        }
    }

    @Override
    public DSignType getType() {
        return type;
    }

}
