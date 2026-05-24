package HUIT.football;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public
enum Role {
    ADMIN(1),
    KHACH(2),
    NHAN_VIEN(3);
    public final long value;
}
