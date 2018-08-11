/*
 * Copyright (C) 2012-2018 Frank Baumann
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
package de.erethon.dungeonsxl.requirement;

import de.erethon.caliburn.CaliburnAPI;
import de.erethon.caliburn.item.ExItem;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public class ForbiddenItemsRequirement extends Requirement {

    CaliburnAPI caliburn = CaliburnAPI.getInstance();

    private RequirementType type = RequirementTypeDefault.FORBIDDEN_ITEMS;

    private List<ExItem> forbiddenItems;

    /* Getters and setters */
    /**
     * @return the forbidden items
     */
    public List<ExItem> getForbiddenItems() {
        return forbiddenItems;
    }

    @Override
    public RequirementType getType() {
        return type;
    }

    /* Actions */
    @Override
    public void setup(ConfigurationSection config) {
        caliburn.deserializeExItemList(config, "forbiddenItems");
    }

    @Override
    public boolean check(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            ExItem exItem = caliburn.getExItem(item);
            if (forbiddenItems.contains(exItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void demand(Player player) {
    }

}
