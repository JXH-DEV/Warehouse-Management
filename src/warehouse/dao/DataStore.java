package warehouse.dao;

import warehouse.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {
    private static DataStore instance;

    private DataStore() {
        try {
            DatabaseConnection.testConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Nuk u lidh me databazën: " + e.getMessage(), e);
        }
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public static void testConnection() throws SQLException {
        DatabaseConnection.testConnection();
    }

    private static LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static java.sql.Date toSqlDate(LocalDate date) {
        return date == null ? null : java.sql.Date.valueOf(date);
    }

    private static IllegalStateException dbError(SQLException e) {
        return new IllegalStateException("Gabim databaze: " + e.getMessage(), e);
    }

    private int nextId(Connection conn, String table) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM " + table;
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("emri_perdoruesit"),
                rs.getString("fjalekalimi"),
                rs.getString("emri_plote"),
                DbEnumMapper.roleFromDb(rs.getString("roli")));
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        String desc = rs.getString("pershkrimi");
        return new Product(
                rs.getInt("id"),
                rs.getString("emri"),
                desc != null ? desc : "",
                rs.getDouble("cmimi"),
                rs.getInt("sasia"),
                DbEnumMapper.categoryFromDb(rs.getString("kategoria")),
                rs.getString("njesia"),
                rs.getInt("stoku_minimal"));
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        String notes = rs.getString("shenime");
        return new Order(
                rs.getInt("id"),
                rs.getString("numri_porosise"),
                toLocalDate(rs.getDate("data_porosise")),
                toLocalDate(rs.getDate("data_dorezimit")),
                DbEnumMapper.orderStatusFromDb(rs.getString("statusi")),
                DbEnumMapper.orderTypeFromDb(rs.getString("lloji")),
                rs.getString("furnizuesi_ose_klienti"),
                notes != null ? notes : "",
                rs.getInt("krijuar_nga_id"));
    }

    private OrderItem mapOrderItem(ResultSet rs, Product product) throws SQLException {
        return new OrderItem(
                rs.getInt("id"),
                rs.getInt("porosia_id"),
                product,
                rs.getInt("sasia"),
                rs.getDouble("cmimi_njesi"));
    }

    private Shipment mapShipment(ResultSet rs, Order order) throws SQLException {
        String notes = rs.getString("shenime");
        return new Shipment(
                rs.getInt("id"),
                rs.getString("numri_gjurmimit"),
                order,
                toLocalDate(rs.getDate("data_dergeses")),
                toLocalDate(rs.getDate("data_vleresuar_mbrritje")),
                DbEnumMapper.shipmentStatusFromDb(rs.getString("statusi")),
                rs.getString("transportuesi"),
                rs.getString("destinacioni"),
                rs.getDouble("pesha"),
                notes != null ? notes : "");
    }

    private Map<Integer, Product> loadProductMap(Connection conn) throws SQLException {
        Map<Integer, Product> map = new HashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM produktet ORDER BY id")) {
            while (rs.next()) {
                Product p = mapProduct(rs);
                map.put(p.getId(), p);
            }
        }
        return map;
    }

    private void loadOrderItems(Connection conn, List<Order> orders, Map<Integer, Product> products) throws SQLException {
        Map<Integer, Order> orderMap = new HashMap<>();
        for (Order o : orders) orderMap.put(o.getId(), o);

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM artikujt_porosise ORDER BY id")) {
            while (rs.next()) {
                Order order = orderMap.get(rs.getInt("porosia_id"));
                if (order == null) continue;
                Product product = products.get(rs.getInt("produkti_id"));
                if (product == null) continue;
                order.addItem(mapOrderItem(rs, product));
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, stoku_aplikuar FROM porosite")) {
            while (rs.next()) {
                Order order = orderMap.get(rs.getInt("id"));
                if (order != null) order.setStockApplied(rs.getBoolean("stoku_aplikuar"));
            }
        }
    }

    public List<User> getAllUsers() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM perdoruesit ORDER BY id")) {
            List<User> list = new ArrayList<>();
            while (rs.next()) list.add(mapUser(rs));
            return list;
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Optional<User> findUserByCredentials(String username, String password) {
        String sql = "SELECT * FROM perdoruesit WHERE emri_perdoruesit = ? AND fjalekalimi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Optional<User> findUserById(int id) {
        String sql = "SELECT * FROM perdoruesit WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void addUser(User user) {
        String sql = "INSERT INTO perdoruesit (id, emri_perdoruesit, fjalekalimi, emri_plote, roli) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getFullName());
            ps.setString(5, DbEnumMapper.toDb(user.getRole()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE perdoruesit SET fjalekalimi = ?, emri_plote = ?, roli = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getPassword());
            ps.setString(2, user.getFullName());
            ps.setString(3, DbEnumMapper.toDb(user.getRole()));
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void removeUser(int id) {
        User user = findUserById(id).orElseThrow(() -> new IllegalStateException("Përdoruesi nuk u gjet."));
        if (user.getRole() == User.Role.ADMIN && countAdmins() <= 1) {
            throw new IllegalStateException("Nuk mund të fshihet admini i fundit.");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM perdoruesit WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public int nextUserId() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return nextId(conn, "perdoruesit");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public long countAdmins() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM perdoruesit WHERE roli = 'ADMIN'")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Product> getAllProducts() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM produktet ORDER BY id")) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(mapProduct(rs));
            return list;
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Optional<Product> findProductById(int id) {
        String sql = "SELECT * FROM produktet WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapProduct(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Product> searchProducts(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return getAllProducts();
        String sql = "SELECT * FROM produktet WHERE LOWER(emri) LIKE ? OR LOWER(COALESCE(pershkrimi, '')) LIKE ? ORDER BY id";
        String pattern = "%" + q + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(mapProduct(rs));
                return list;
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Product> getLowStockProducts() {
        return getAllProducts().stream().filter(Product::isLowStock).collect(Collectors.toList());
    }

    public boolean isProductInUse(int productId) {
        String sql = "SELECT 1 FROM artikujt_porosise WHERE produkti_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO produktet (id, emri, pershkrimi, cmimi, sasia, kategoria, njesia, stoku_minimal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setString(6, DbEnumMapper.toDb(product.getCategory()));
            ps.setString(7, product.getUnit());
            ps.setInt(8, product.getMinStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE produktet SET emri = ?, pershkrimi = ?, cmimi = ?, sasia = ?, kategoria = ?, njesia = ?, stoku_minimal = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setString(5, DbEnumMapper.toDb(product.getCategory()));
            ps.setString(6, product.getUnit());
            ps.setInt(7, product.getMinStock());
            ps.setInt(8, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void removeProduct(int id) {
        if (isProductInUse(id)) throw new IllegalStateException("Produkti përdoret në porosi dhe nuk mund të fshihet.");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM produktet WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public int nextProductId() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return nextId(conn, "produktet");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Order> getAllOrders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Map<Integer, Product> products = loadProductMap(conn);
            List<Order> orders = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM porosite ORDER BY data_porosise DESC, id DESC")) {
                while (rs.next()) orders.add(mapOrder(rs));
            }
            loadOrderItems(conn, orders, products);
            return orders;
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Optional<Order> findOrderById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try {
                return Optional.of(reloadOrder(conn, id));
            } catch (IllegalStateException e) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public List<Order> getOrdersByType(Order.Type type) {
        return getAllOrders().stream().filter(o -> o.getType() == type).collect(Collectors.toList());
    }

    public void addOrder(Order order) {
        try {
            insertOrder(null, order);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public static final class NewOrderLine {
        public final int productId;
        public final int quantity;
        public final double unitPrice;

        public NewOrderLine(int productId, int quantity, double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    public void createOrderWithItems(Order order, List<NewOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalStateException("Porosia duhet të ketë të paktën një artikull.");
        }
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            insertOrder(conn, order);
            int nextItemId = nextId(conn, "artikujt_porosise");
            String itemSql = "INSERT INTO artikujt_porosise (id, porosia_id, produkti_id, sasia, cmimi_njesi) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (NewOrderLine line : lines) {
                    if (line.quantity <= 0) {
                        throw new IllegalStateException("Sasia duhet të jetë pozitive.");
                    }
                    if (!findProductById(line.productId).isPresent()) {
                        throw new IllegalStateException("Produkti nuk u gjet.");
                    }
                    ps.setInt(1, nextItemId++);
                    ps.setInt(2, order.getId());
                    ps.setInt(3, line.productId);
                    ps.setInt(4, line.quantity);
                    ps.setDouble(5, line.unitPrice);
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("unique") || msg.contains("duplicate")) {
                throw new IllegalStateException("Ky produkt ekziston tashmë në porosi.", e);
            }
            throw dbError(e);
        } finally {
            restoreAutoCommit(conn);
            closeQuietly(conn);
        }
    }

    private void insertOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO porosite (id, numri_porosise, data_porosise, data_dorezimit, statusi, lloji, furnizuesi_ose_klienti, shenime, krijuar_nga_id, stoku_aplikuar) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if (conn == null) {
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                bindOrderInsert(ps, order);
                ps.executeUpdate();
            }
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindOrderInsert(ps, order);
            ps.executeUpdate();
        }
    }

    private void bindOrderInsert(PreparedStatement ps, Order order) throws SQLException {
        ps.setInt(1, order.getId());
        ps.setString(2, order.getOrderNumber());
        ps.setDate(3, toSqlDate(order.getOrderDate()));
        ps.setDate(4, toSqlDate(order.getDeliveryDate()));
        ps.setString(5, DbEnumMapper.toDb(order.getStatus()));
        ps.setString(6, DbEnumMapper.toDb(order.getType()));
        ps.setString(7, order.getSupplierOrClient());
        ps.setString(8, order.getNotes());
        ps.setInt(9, order.getCreatedByUserId());
        ps.setBoolean(10, order.isStockApplied());
    }

    public void removeOrder(int id) {
        Order order = findOrderById(id).orElseThrow(() -> new IllegalStateException("Porosia nuk u gjet."));
        if (hasShipmentsForOrder(id)) throw new IllegalStateException("Porosia ka dërgesa të lidhura dhe nuk mund të fshihet.");

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            if (order.isStockApplied()) {
                persistReverseStock(conn, order);
                updateStockApplied(conn, id, false);
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM porosite WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw dbError(e);
        } finally {
            restoreAutoCommit(conn);
            closeQuietly(conn);
        }
    }

    public boolean hasShipmentsForOrder(int orderId) {
        String sql = "SELECT 1 FROM dergesat WHERE porosia_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void addOrderItem(int orderId, int productId, int quantity, double unitPrice) {
        Order order = findOrderById(orderId).orElseThrow(() -> new IllegalStateException("Porosia nuk u gjet."));
        if (order.getStatus() == Order.Status.DELIVERED || order.getStatus() == Order.Status.CANCELLED) {
            throw new IllegalStateException("Porosia e dorëzuar ose e anuluar nuk mund të ndryshohet.");
        }
        if (quantity <= 0) throw new IllegalStateException("Sasia duhet të jetë pozitive.");
        if (!findProductById(productId).isPresent()) throw new IllegalStateException("Produkti nuk u gjet.");

        int itemId = nextOrderItemId();
        String sql = "INSERT INTO artikujt_porosise (id, porosia_id, produkti_id, sasia, cmimi_njesi) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, orderId);
            ps.setInt(3, productId);
            ps.setInt(4, quantity);
            ps.setDouble(5, unitPrice);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void removeOrderItem(int orderId, int itemId) {
        Order order = findOrderById(orderId).orElseThrow(() -> new IllegalStateException("Porosia nuk u gjet."));
        if (order.getStatus() == Order.Status.DELIVERED || order.getStatus() == Order.Status.CANCELLED) {
            throw new IllegalStateException("Porosia e dorëzuar ose e anuluar nuk mund të ndryshohet.");
        }
        String sql = "DELETE FROM artikujt_porosise WHERE id = ? AND porosia_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, orderId);
            if (ps.executeUpdate() == 0) throw new IllegalStateException("Artikulli nuk u gjet.");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void updateOrderStatus(Order order, Order.Status oldStatus, Order.Status newStatus) {
        if (oldStatus == newStatus) return;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Order fresh = reloadOrder(conn, order.getId());
            if (newStatus == Order.Status.DELIVERED && !fresh.isStockApplied()) {
                persistApplyStock(conn, fresh);
                updateStockApplied(conn, fresh.getId(), true);
                fresh.setStockApplied(true);
            } else if (oldStatus == Order.Status.DELIVERED && newStatus != Order.Status.DELIVERED && fresh.isStockApplied()) {
                persistReverseStock(conn, fresh);
                updateStockApplied(conn, fresh.getId(), false);
                fresh.setStockApplied(false);
            }

            try (PreparedStatement ps = conn.prepareStatement("UPDATE porosite SET statusi = ? WHERE id = ?")) {
                ps.setString(1, DbEnumMapper.toDb(newStatus));
                ps.setInt(2, order.getId());
                ps.executeUpdate();
            }
            conn.commit();
            order.setStatus(newStatus);
            if (newStatus == Order.Status.DELIVERED) order.setStockApplied(true);
            else if (oldStatus == Order.Status.DELIVERED) order.setStockApplied(false);
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw dbError(e);
        } finally {
            restoreAutoCommit(conn);
            closeQuietly(conn);
        }
    }

    public void applyStock(Order order) {
        if (order.getItems().isEmpty()) throw new IllegalStateException("Porosia nuk ka artikuj.");
        if (order.getType() == Order.Type.OUTGOING) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct().getQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Stoku i pamjaftueshëm për produktin: " + item.getProduct().getName());
                }
            }
            for (OrderItem item : order.getItems()) {
                Product p = item.getProduct();
                p.setQuantity(p.getQuantity() - item.getQuantity());
            }
        } else {
            for (OrderItem item : order.getItems()) {
                Product p = item.getProduct();
                p.setQuantity(p.getQuantity() + item.getQuantity());
            }
        }
    }

    public void reverseStock(Order order) {
        if (order.getType() == Order.Type.OUTGOING) {
            for (OrderItem item : order.getItems()) {
                Product p = item.getProduct();
                p.setQuantity(p.getQuantity() + item.getQuantity());
            }
        } else {
            for (OrderItem item : order.getItems()) {
                Product p = item.getProduct();
                p.setQuantity(p.getQuantity() - item.getQuantity());
            }
        }
    }

    private Order reloadOrder(Connection conn, int orderId) throws SQLException {
        Map<Integer, Product> products = loadProductMap(conn);
        Order order;
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM porosite WHERE id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Porosia nuk u gjet.");
                order = mapOrder(rs);
            }
        }
        List<Order> single = Collections.singletonList(order);
        loadOrderItems(conn, single, products);
        return order;
    }

    private void persistApplyStock(Connection conn, Order order) throws SQLException {
        if (order.getItems().isEmpty()) throw new IllegalStateException("Porosia nuk ka artikuj.");
        String selectQty = "SELECT sasia FROM produktet WHERE id = ? FOR UPDATE";
        String updateQty = "UPDATE produktet SET sasia = ? WHERE id = ?";

        for (OrderItem item : order.getItems()) {
            int productId = item.getProduct().getId();
            int currentQty;
            try (PreparedStatement ps = conn.prepareStatement(selectQty)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalStateException("Produkti nuk u gjet.");
                    currentQty = rs.getInt("sasia");
                }
            }
            int newQty;
            if (order.getType() == Order.Type.OUTGOING) {
                if (currentQty < item.getQuantity()) {
                    throw new IllegalStateException("Stoku i pamjaftueshëm për produktin: " + item.getProduct().getName());
                }
                newQty = currentQty - item.getQuantity();
            } else {
                newQty = currentQty + item.getQuantity();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateQty)) {
                ps.setInt(1, newQty);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }
            item.getProduct().setQuantity(newQty);
        }
    }

    private void persistReverseStock(Connection conn, Order order) throws SQLException {
        String selectQty = "SELECT sasia FROM produktet WHERE id = ? FOR UPDATE";
        String updateQty = "UPDATE produktet SET sasia = ? WHERE id = ?";

        for (OrderItem item : order.getItems()) {
            int productId = item.getProduct().getId();
            int currentQty;
            try (PreparedStatement ps = conn.prepareStatement(selectQty)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalStateException("Produkti nuk u gjet.");
                    currentQty = rs.getInt("sasia");
                }
            }
            int newQty = order.getType() == Order.Type.OUTGOING
                    ? currentQty + item.getQuantity()
                    : currentQty - item.getQuantity();
            if (newQty < 0) throw new IllegalStateException("Stoku nuk mund të jetë negativ.");
            try (PreparedStatement ps = conn.prepareStatement(updateQty)) {
                ps.setInt(1, newQty);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }
            item.getProduct().setQuantity(newQty);
        }
    }

    private void updateStockApplied(Connection conn, int orderId, boolean applied) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE porosite SET stoku_aplikuar = ? WHERE id = ?")) {
            ps.setBoolean(1, applied);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public int nextOrderId() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return nextId(conn, "porosite");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public int nextOrderItemId() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return nextId(conn, "artikujt_porosise");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public String generateOrderNumber(int orderId) {
        return "ORD-" + String.format("%03d", orderId);
    }

    public List<Shipment> getAllShipments() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Map<Integer, Order> orderShells = new HashMap<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, numri_porosise, data_porosise, data_dorezimit, statusi, lloji, furnizuesi_ose_klienti, shenime, krijuar_nga_id FROM porosite")) {
                while (rs.next()) {
                    Order o = mapOrder(rs);
                    orderShells.put(o.getId(), o);
                }
            }
            List<Shipment> list = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM dergesat ORDER BY data_dergeses DESC, id DESC")) {
                while (rs.next()) {
                    Order order = orderShells.get(rs.getInt("porosia_id"));
                    if (order == null) continue;
                    list.add(mapShipment(rs, order));
                }
            }
            return list;
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Optional<Shipment> findShipmentById(int id) {
        return getAllShipments().stream().filter(s -> s.getId() == id).findFirst();
    }

    public void addShipment(Shipment shipment) {
        String sql = "INSERT INTO dergesat (id, numri_gjurmimit, porosia_id, data_dergeses, data_vleresuar_mbrritje, statusi, transportuesi, destinacioni, pesha, shenime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shipment.getId());
            ps.setString(2, shipment.getTrackingNumber());
            ps.setInt(3, shipment.getOrder().getId());
            ps.setDate(4, toSqlDate(shipment.getShipDate()));
            ps.setDate(5, toSqlDate(shipment.getEstimatedArrival()));
            ps.setString(6, DbEnumMapper.toDb(shipment.getStatus()));
            ps.setString(7, shipment.getCarrier());
            ps.setString(8, shipment.getDestination());
            ps.setDouble(9, shipment.getWeight());
            ps.setString(10, shipment.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void updateShipment(Shipment shipment) {
        String sql = "UPDATE dergesat SET numri_gjurmimit = ?, porosia_id = ?, data_dergeses = ?, data_vleresuar_mbrritje = ?, statusi = ?, transportuesi = ?, destinacioni = ?, pesha = ?, shenime = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shipment.getTrackingNumber());
            ps.setInt(2, shipment.getOrder().getId());
            ps.setDate(3, toSqlDate(shipment.getShipDate()));
            ps.setDate(4, toSqlDate(shipment.getEstimatedArrival()));
            ps.setString(5, DbEnumMapper.toDb(shipment.getStatus()));
            ps.setString(6, shipment.getCarrier());
            ps.setString(7, shipment.getDestination());
            ps.setDouble(8, shipment.getWeight());
            ps.setString(9, shipment.getNotes());
            ps.setInt(10, shipment.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void removeShipment(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM dergesat WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public int nextShipmentId() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return nextId(conn, "dergesat");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis();
    }

    public int getTotalProducts() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produktet")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public int getTotalOrders() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM porosite")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public int getTotalShipments() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dergesat")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public double getTotalInventoryValue() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(cmimi * sasia), 0) FROM produktet")) {
            rs.next();
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public long getPendingOrdersCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM porosite WHERE statusi IN ('NE_PRITJE', 'KONFIRMUAR')")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public long getActiveShipmentsCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dergesat WHERE statusi IN ('NE_TRANSIT', 'DERGUAAR')")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public static final class RecentOrderRow {
        public final String orderNumber;
        public final Order.Type type;
        public final String supplierOrClient;
        public final Order.Status status;
        public final double totalAmount;

        public RecentOrderRow(String orderNumber, Order.Type type, String supplierOrClient,
                Order.Status status, double totalAmount) {
            this.orderNumber = orderNumber;
            this.type = type;
            this.supplierOrClient = supplierOrClient;
            this.status = status;
            this.totalAmount = totalAmount;
        }
    }

    public static final class DashboardSnapshot {
        public final int totalProducts;
        public final long pendingOrders;
        public final long activeShipments;
        public final int totalOrders;
        public final double inventoryValue;
        public final List<Product> lowStock;
        public final List<RecentOrderRow> recentOrders;

        public DashboardSnapshot(int totalProducts, long pendingOrders, long activeShipments,
                int totalOrders, double inventoryValue, List<Product> lowStock,
                List<RecentOrderRow> recentOrders) {
            this.totalProducts = totalProducts;
            this.pendingOrders = pendingOrders;
            this.activeShipments = activeShipments;
            this.totalOrders = totalOrders;
            this.inventoryValue = inventoryValue;
            this.lowStock = lowStock;
            this.recentOrders = recentOrders;
        }
    }

    public DashboardSnapshot getDashboardSnapshot() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalProducts = queryInt(conn, "SELECT COUNT(*) FROM produktet");
            long pendingOrders = queryLong(conn,
                    "SELECT COUNT(*) FROM porosite WHERE statusi IN ('NE_PRITJE', 'KONFIRMUAR')");
            long activeShipments = queryLong(conn,
                    "SELECT COUNT(*) FROM dergesat WHERE statusi IN ('NE_TRANSIT', 'DERGUAAR')");
            int totalOrders = queryInt(conn, "SELECT COUNT(*) FROM porosite");
            double inventoryValue = queryDouble(conn,
                    "SELECT COALESCE(SUM(cmimi * sasia), 0) FROM produktet");

            List<Product> lowStock = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT * FROM produktet WHERE sasia <= stoku_minimal ORDER BY sasia, id")) {
                while (rs.next()) lowStock.add(mapProduct(rs));
            }

            List<RecentOrderRow> recentOrders = new ArrayList<>();
            String recentSql = "SELECT p.numri_porosise, p.lloji, p.furnizuesi_ose_klienti, p.statusi, "
                    + "COALESCE(SUM(a.sasia * a.cmimi_njesi), 0) AS total "
                    + "FROM porosite p "
                    + "LEFT JOIN artikujt_porosise a ON a.porosia_id = p.id "
                    + "GROUP BY p.id, p.numri_porosise, p.lloji, p.furnizuesi_ose_klienti, p.statusi, p.data_porosise "
                    + "ORDER BY p.data_porosise DESC, p.id DESC LIMIT 8";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(recentSql)) {
                while (rs.next()) {
                    recentOrders.add(new RecentOrderRow(
                            rs.getString("numri_porosise"),
                            DbEnumMapper.orderTypeFromDb(rs.getString("lloji")),
                            rs.getString("furnizuesi_ose_klienti"),
                            DbEnumMapper.orderStatusFromDb(rs.getString("statusi")),
                            rs.getDouble("total")));
                }
            }

            return new DashboardSnapshot(totalProducts, pendingOrders, activeShipments, totalOrders,
                    inventoryValue, lowStock, recentOrders);
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    private static int queryInt(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static long queryLong(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static double queryDouble(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    private static void restoreAutoCommit(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
