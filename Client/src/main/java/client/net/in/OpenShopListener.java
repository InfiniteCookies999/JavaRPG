package client.net.in;

import java.io.IOException;

import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.OpenShopListener.OpenShopPacket;
import lombok.AllArgsConstructor;

public class OpenShopListener extends PacketListener<OpenShopPacket> {

	@AllArgsConstructor
	private class ShopItemData {
		private int itemId;
		private int amount;
		private int price;
	}
	
	@AllArgsConstructor
	class OpenShopPacket extends PacketData {
		private ShopItemData[] shopItems;
	}
	
	public OpenShopListener() {
		super(NetOpcodes.OPEN_SHOP);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int numShopItems = handle.readUnsignedByte();
		ShopItemData[] shopItems = new ShopItemData[numShopItems];
		
		for (int i = 0; i < numShopItems; i++) {
			int itemId = handle.readUnsignedInt24();
			int amount = handle.readInt();
			int price = handle.readInt();
			shopItems[i] = new ShopItemData(itemId, amount, price);
		}
		
		return new OpenShopPacket(shopItems);
	}

	@Override
	public void onEvent(OpenShopPacket packet) {
		System.out.println("Opening shop!!");
		for (int i = 0; i < packet.shopItems.length; i++) {
			ShopItemData shopItem = packet.shopItems[i];
			System.out.println("id: " + shopItem.itemId);
			System.out.println("amount: " + shopItem.amount);
			System.out.println("price: " + shopItem.price);
		}
	}
}
