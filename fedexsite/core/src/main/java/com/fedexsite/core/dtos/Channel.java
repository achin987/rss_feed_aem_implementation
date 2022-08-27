package com.fedexsite.core.dtos;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Channel {
  private List<Item> item;
}
