package cn.nu11cat.train.common.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResp<T> {
    private boolean success = true;
    private String message;
    private T content;

    public CommonResp(T content) {
        this.content = content;
    }
}
