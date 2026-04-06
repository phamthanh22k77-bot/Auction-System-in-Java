package model;

public abstract class Entity implements java.io.Serializable {
    private final String id;

    public Entity(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty"); // ném ra lỗi nếu id rỗng
        }
        this.id = id;
    }

    public Entity() {
        this.id = java.util.UUID.randomUUID().toString(); // id ngẫu nhiên không trùng lặp
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true; // kiểm tra xem có cùng địa chỉ ô nhớ không
        if (o == null || getClass() != o.getClass())
            return false; // kiểm tra xem có cùng class không
        Entity entity = (Entity) o; // ép kiểu để có thể get Id
        return java.util.Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "'}";
    }
}