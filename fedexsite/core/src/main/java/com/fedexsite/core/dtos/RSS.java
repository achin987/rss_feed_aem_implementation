package com.fedexsite.core.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RSS {
  private Channel channel;
}
