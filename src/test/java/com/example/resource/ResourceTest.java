package com.example.resource;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import io.github.cdiunit.resource.SupportResource;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses(ResourceProducer.class)
@SupportResource
class ResourceTest {

    AResourceType _unnamedAResource;
    AResourceType _namedAResource;
    AResource _typedAResource;

    @Resource
    void unnamedAResource(AResourceType resource) {
        _unnamedAResource = resource;
    }

    @Resource
    BResourceType unnamedBResource;

    @Resource(name = "namedAResource")
    void withNamedAResource(AResourceType resource) {
        _namedAResource = resource;
    }

    @Resource(name = "namedBResource")
    BResourceType namedBResource;

    @Resource
    public void setTypedAResource(AResource resource) {
        _typedAResource = resource;
    }

    @Resource
    BResource typedBResource;

    @Test
    void testResourceSupport() {
        assertNotEquals(_unnamedAResource, _namedAResource);
        assertNotEquals(unnamedBResource, namedBResource);
        assertTrue(_unnamedAResource instanceof AResource);
        assertTrue(unnamedBResource instanceof BResource);
        assertTrue(_namedAResource instanceof AResource);
        assertTrue(namedBResource instanceof BResource);
        assertTrue(_typedAResource instanceof AResourceExt);
        assertTrue(typedBResource instanceof BResourceExt);
    }

}