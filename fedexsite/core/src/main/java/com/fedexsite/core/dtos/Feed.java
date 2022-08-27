package com.fedexsite.core.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Feed {
  private RSS rss;
}
