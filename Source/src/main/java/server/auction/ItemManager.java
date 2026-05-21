package server.auction;

import server.dao.ItemDAO;
import server.models.item.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ItemManager - Quản lý danh sách Item trong bộ nhớ, đồng bộ với ItemDAO.
 */
public class ItemManager {
    private static volatile ItemManager instance;
    private final List<Item> items = new CopyOnWriteArrayList<>();
    private final ItemDAO dao = new ItemDAO();

    private ItemManager() {
    }

    public static ItemManager getInstance() {
        if (instance == null) {
            synchronized (ItemManager.class) {
                if (instance == null) {
                    instance = new ItemManager();
                }
            }
        }
        return instance;
    }

    public synchronized void khoiDong() throws IOException {
        items.clear();
        items.addAll(dao.loadAll());
        System.out.println("[ItemManager] Đã nạp " + items.size() + " vật phẩm.");
    }

    public List<Item> getItems() {
        return items;
    }

    public Item timTheoId(String id) {
        for (Item i : items) {
            if (i.getId().equals(id))
                return i;
        }
        return null;
    }

    public void themItem(Item item) throws IOException {
        items.add(item);
        dao.them(item);
    }
}
