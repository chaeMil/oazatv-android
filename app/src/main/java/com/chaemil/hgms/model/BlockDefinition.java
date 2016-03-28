package com.chaemil.hgms.model;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by chaemil on 28.3.16.
 */
public class BlockDefinition extends SugarRecord {

    private Long id;
    private String name;
    private String definition;

    public BlockDefinition() {
    }

    public BlockDefinition(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public static BlockDefinition findByName(String name) {
        List<BlockDefinition> definitions = BlockDefinition.find(BlockDefinition.class, "name = ?", String.valueOf(name));
        if (definitions.size() <= 0) {
            return null;
        } else {
            return definitions.get(0);
        }
    }
}
