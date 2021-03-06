package mcjty.efab.compat.jei;

import mcjty.efab.blocks.ModBlocks;
import mcjty.efab.blocks.crafter.CrafterContainer;
import mcjty.efab.blocks.crafter.CrafterTE;
import mcjty.efab.blocks.grid.GridContainer;
import mcjty.efab.compat.jei.grid.GridRecipeCategory;
import mcjty.efab.compat.jei.grid.GridRecipeWrapperFactory;
import mcjty.efab.config.GeneralConfiguration;
import mcjty.efab.network.EFabMessages;
import mcjty.efab.network.PacketSendRecipe;
import mcjty.efab.recipes.IEFabRecipe;
import mcjty.efab.recipes.RecipeManager;
import mcjty.efab.tools.ItemStackList;
import mezz.jei.api.*;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JEIPlugin
public class EFabJeiPlugin implements IModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.handleRecipes(IEFabRecipe.class, new GridRecipeWrapperFactory(), GridRecipeCategory.ID);

        List<IEFabRecipe> efabRecipes = RecipeManager.getRecipes().stream().map(JEIRecipeAdapter::new).collect(Collectors.toList());
        registry.addRecipes(efabRecipes, GridRecipeCategory.ID);
        IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();

        if (GeneralConfiguration.vanillaCraftingAllowed) {
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.gridBlock), GridRecipeCategory.ID, VanillaRecipeCategoryUid.CRAFTING);
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.crafterBlock), GridRecipeCategory.ID, VanillaRecipeCategoryUid.CRAFTING);
            transferRegistry.addRecipeTransferHandler(GridContainer.class, VanillaRecipeCategoryUid.CRAFTING, GridContainer.SLOT_CRAFTINPUT, 9, GridContainer.SLOT_GHOSTOUT + 1, 36);
        } else {
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.gridBlock), GridRecipeCategory.ID);
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.crafterBlock), GridRecipeCategory.ID);
        }

        transferRegistry.addRecipeTransferHandler(GridContainer.class, GridRecipeCategory.ID, GridContainer.SLOT_CRAFTINPUT, 9, GridContainer.SLOT_GHOSTOUT + 1, 36);

        IRecipeTransferHandler<CrafterContainer> handler = new IRecipeTransferHandler<CrafterContainer>() {
            @Override
            public Class<CrafterContainer> getContainerClass() {
                return CrafterContainer.class;
            }

            @Nullable
            @Override
            public IRecipeTransferError transferRecipe(CrafterContainer container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
                Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

                IInventory inventory = container.getInventory(CrafterContainer.CONTAINER_INVENTORY);
                BlockPos pos = ((CrafterTE) inventory).getPos();

                if (doTransfer) {
                    sendIngredients(guiIngredients, pos);
                }

                return null;
            }
        };
        transferRegistry.addRecipeTransferHandler(handler, GridRecipeCategory.ID);
        transferRegistry.addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IJeiHelpers helpers = registry.getJeiHelpers();
        IGuiHelper guiHelper = helpers.getGuiHelper();

        registry.addRecipeCategories(new GridRecipeCategory(guiHelper));
    }

    public static void sendIngredients(Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        ItemStackList items = ItemStackList.create(10);
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : guiIngredients.entrySet()) {
            int recipeSlot = entry.getKey();
            List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
            if (!allIngredients.isEmpty()) {
                items.set(recipeSlot, allIngredients.get(0));
            }
        }

        EFabMessages.INSTANCE.sendToServer(new PacketSendRecipe(items, pos));
    }

}
