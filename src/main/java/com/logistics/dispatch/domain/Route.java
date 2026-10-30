package com.logistics.dispatch.domain;

import java.util.UUID;

/** Minimal aggregate skeleton; lifecycle rules will be added with the domain implementation. */
public record Route(UUID id, String status) {}
