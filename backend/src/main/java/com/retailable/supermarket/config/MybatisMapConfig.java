package com.retailable.supermarket.config;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * MyBatis applies underscore-to-camel-case mapping to beans, but its default
 * MapWrapper keeps database column labels unchanged. API projections in this
 * application intentionally use maps, so normalize their keys at the mapping
 * boundary instead of leaking snake_case through the JSON API.
 */
@Configuration
public class MybatisMapConfig {
    @Bean
    ConfigurationCustomizer camelCaseMapCustomizer() {
        return configuration -> configuration.setObjectWrapperFactory(new CamelCaseMapWrapperFactory());
    }

    static final class CamelCaseMapWrapperFactory implements ObjectWrapperFactory {
        @Override
        public boolean hasWrapperFor(Object object) {
            return object instanceof Map;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
            return new CamelCaseMapWrapper(metaObject, (Map<String, Object>) object);
        }
    }

    static final class CamelCaseMapWrapper extends MapWrapper {
        CamelCaseMapWrapper(MetaObject metaObject, Map<String, Object> map) {
            super(metaObject, map);
        }

        @Override
        public String findProperty(String name, boolean useCamelCaseMapping) {
            if (!useCamelCaseMapping || name.indexOf('_') < 0) return name;
            StringBuilder result = new StringBuilder(name.length());
            boolean upperNext = false;
            for (char character : name.toCharArray()) {
                if (character == '_') {
                    upperNext = true;
                } else {
                    result.append(upperNext ? Character.toUpperCase(character) : character);
                    upperNext = false;
                }
            }
            return result.toString();
        }
    }
}
