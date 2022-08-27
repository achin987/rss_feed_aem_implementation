package com.fedexsite.core.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Guid {
  private boolean isPermaLink;
  private int content;
}
