package com.geborskimateusz.api.composite.movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ServiceAddresses {
    private final String cmp;
    private final String mov;
    private final String rev;
    private final String rec;

    public ServiceAddresses() {
        cmp = null;
        mov = null;
        rev = null;
        rec = null;
    }
}
