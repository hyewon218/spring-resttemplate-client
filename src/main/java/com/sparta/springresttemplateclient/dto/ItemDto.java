package com.sparta.springresttemplateclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@Getter
@NoArgsConstructor
public class ItemDto {
    private String title;
    private int price;

    // Object item -> {"title":"Mac","price":3888000} 한 줄씩만 넘어감.
    public ItemDto(JSONObject itemJson) {
        this.title = itemJson.getString("title"); // Mac 이 가져와져서 필드 title 에 담긴다.
        this.price = itemJson.getInt("price"); // 3888000 이 가져와져서 필드 title 에 담긴다.
    }
}

