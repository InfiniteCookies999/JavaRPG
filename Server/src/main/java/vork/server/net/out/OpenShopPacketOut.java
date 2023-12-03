package vork.server.net.out;

import vork.server.game.Shop;
import vork.server.game.container.ItemStack;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class OpenShopPacketOut extends PacketOut {

	private Shop shop;
	
	public OpenShopPacketOut(Shop shop) {
		super(NetOpcodes.OPEN_SHOP);
		this.shop = shop;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		// 3 + 4 + 4
		// 1024 / 11 = 93 items approx.
		handle.writeUnsignedByte(shop.getItems().size());
		for (Shop.ShopItem shopItem : shop.getItems()) {
			ItemStack sellItem = shopItem.getItem();
			int price = shopItem.getPrice();
			handle.writeUnsignedInt24(sellItem.getId());
			handle.writeInt(sellItem.getAmount());
			handle.writeInt(price);
		}
	}
}
