package com.fedexsite.core.dtos;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Item {
  @SerializedName("a10:updated")
  private String updated;
  private String link;
  private String description;
  private String title;
  private Guid guid;
}
