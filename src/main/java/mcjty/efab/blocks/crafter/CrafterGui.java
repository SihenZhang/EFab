package mcjty.efab.blocks.crafter;

import mcjty.efab.EFab;
import mcjty.efab.network.EFabMessages;
import mcjty.efab.network.PacketGetGridStatus;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;
import java.util.List;

public class CrafterGui extends GenericGuiContainer<CrafterTE> {

    public static final int WIDTH = 171;
    public static final int HEIGHT = 192;

    private Button leftArrow;
    private Button rightArrow;
    private Label errorLabel;
    private TextField nameField;

    private static final ResourceLocation mainBackground = new ResourceLocation(EFab.MODID, "textures/gui/crafter.png");

    public CrafterGui(CrafterTE controller, CrafterContainer container) {
        super(EFab.instance, EFabMessages.INSTANCE, controller, container, 0, "crafter");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout())
                .setBackground(mainBackground);

        leftArrow = new Button(mc, this)
                .setText("<")
                .setLayoutHint(new PositionalLayout.PositionalHint(82, 45, 13, 18))
                .setVisible(false)
                .addButtonEvent(parent -> left());
        rightArrow = new Button(mc, this)
                .setText(">")
                .setLayoutHint(new PositionalLayout.PositionalHint(112, 45, 13, 18))
                .setVisible(false)
                .addButtonEvent(parent -> right());
        errorLabel = new Label<>(mc, this)
                .setText("")
                .setColor(0xffff0000)
                .setLayoutHint(new PositionalLayout.PositionalHint(6, 70, 160, 20));
        nameField = new TextField(mc, this)
                .setTooltips("If you give this crafter a name", "it will only use item storages", "with the same name")
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 90, 161, 16));
        nameField.setText(tileEntity.getCraftingName());
        nameField.addTextEvent((parent, newText) -> setName());

        toplevel.addChild(leftArrow).addChild(rightArrow).addChild(errorLabel).addChild(nameField);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void left() {
        sendServerCommand(EFabMessages.INSTANCE, CrafterTE.CMD_LEFT);
    }

    private void right() {
        sendServerCommand(EFabMessages.INSTANCE, CrafterTE.CMD_RIGHT);
    }

    private void setName() {
        sendServerCommand(EFabMessages.INSTANCE, CrafterTE.CMD_SETNAME, new Argument("name", nameField.getText()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x1, int x2) {
        EFabMessages.INSTANCE.sendToServer(new PacketGetGridStatus(tileEntity.getPos()));
        String lastError = tileEntity.getLastError();
        errorLabel.setText(lastError);

        List<ItemStack> outputs = tileEntity.getOutputs();
        leftArrow.setVisible(outputs.size() > 1);
        rightArrow.setVisible(outputs.size() > 1);

        drawWindow();
    }
}
