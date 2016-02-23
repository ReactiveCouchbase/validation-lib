package org.reactivecouchbase.validation.test;

import org.junit.Test;
import org.reactivecouchbase.validation.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class PathsTest {

    @Test
    public void testOf() throws Exception {
        assertThat(Paths.Root.field("user").field("name")).isEqualTo(Paths.Root.field("user").field("name"));
        assertThat(Paths.Root.field("user").field("name").hashCode()).isEqualTo(Paths.Root.field("user").field("name").hashCode());
        assertThat(Paths.Root.field("user").field("name").path).isNotNull();
        assertThat(Paths.Root.field("user").field("name").path.size()).isEqualTo(2);
        assertThat(Paths.Root.field("user").field("name").toString()).isEqualTo("/ user / name");
    }

    @Test
    public void testStringify() throws Exception {
        assertThat(Paths.Root.field("user").field("name").toString()).isEqualTo("/ user / name");
    }

    @Test
    public void testToString() throws Exception {
        assertThat(Paths.Root.field("user").field("name").toString()).isEqualTo("/ user / name");
    }

    @Test
    public void testAtIndex() throws Exception {
        assertThat(Paths.Root.field("users").atIndex(2).toString()).isEqualTo("/ users / [2]");
    }

    @Test
    public void testField() throws Exception {
        assertThat(Paths.Root.field("users").atIndex(2).toString()).isEqualTo("/ users / [2]");
    }

    @Test
    public void testAndThenIndex() throws Exception {
        assertThat(Paths.Root.andThen("users").andThen(2).toString()).isEqualTo("/ users / [2]");
    }

    @Test
    public void testAndThenKey() throws Exception {
        assertThat(Paths.Root.andThen("users").andThen(2).toString()).isEqualTo("/ users / [2]");
    }

    @Test
    public void testCompose() throws Exception {
        assertThat(Paths.Root.field("users").atIndex(2).compose(Paths.Root.field("address").field("street")).toString()).isEqualTo("/ users / [2] / address / street");
    }

} 
