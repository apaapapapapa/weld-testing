package com.example.resource;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Named;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.junit5.CdiJUnit5Extension;
import io.github.cdiunit.resource.SupportResource;

@ExtendWith(CdiJUnit5Extension.class)
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
        // public to make it visible as Java Bean property to derive the name
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

    interface AResourceType {
    }

    public interface BResourceType {
    }

    @Vetoed
    public static class AResource implements AResourceType {
    }

    @Vetoed
    static class BResource implements BResourceType {
    }

    @Vetoed
    static class AResourceExt extends AResource {
    }

    @Vetoed
    static class BResourceExt extends BResource {
    }

    @Produces
    @Resource(name = "unnamedAResource")
    AResourceType produceUnnamedAResource = new AResource();

    @Produces
    @Resource(name = "unnamedBResource")
    BResourceType produceUnnamedBResource() {
        return new BResource();
    }

    @Produces
    @Named("namedAResource")
    AResourceType produceNamedAResource = new AResource();

    @Produces
    @Resource
    public BResourceType getNamedBResource() {
        // public to make it visible as Java Bean property to derive the name
        return new BResource();
    }

    @Produces
    @Resource(name = "typedAResource", type = AResource.class)
    AResourceExt produceTypedAResource = new AResourceExt();

    @Produces
    @Resource(name = "typedBResource", type = BResource.class)
    BResourceExt produceTypedBResource() {
        return new BResourceExt();
    }

}