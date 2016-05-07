package org.freedesktop;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class IniStyleFile {

    public enum ValueType {
        LOCALE_STRING,
        STRINGS,
        STRING,
        INTEGERS,
        INTEGER,
        BOOLEAN,
        POINTS,
    }

    protected final Map<String, Map<String, String>> data;
    protected final Map<String, Map<String, ValueType>> knownTypes;

    protected final String defaultGroup;

    public IniStyleFile() {
        this("default");
    }

    public IniStyleFile(String defaultGroup) {
        this.defaultGroup = defaultGroup;
        this.data = new HashMap<>();
        this.knownTypes = new HashMap<>();
    }

    public IniStyleFile(IniStyleFile other) {
        this(other, other.defaultGroup);
    }

    public IniStyleFile(IniStyleFile other, String defaultGroup) {
        // FIXME deep copy the data
        this.defaultGroup = defaultGroup;
        this.data = other.data;
        this.knownTypes = other.knownTypes;
        checkAllValid();
    }

    public void addGroup(String groupName) {
        checkValidGroupName(groupName);

        if (data.containsKey(groupName)) {
            return;
        }

        data.put(groupName, new HashMap<String, String>());

    }

    public void add(String group, String key, String value) {
        checkValidGroupName(group);
        checkValidKeyValue(group, key, value);

        if (!(data.containsKey(group))) {
            addGroup(group);
        }

        Map<String, String> groupData = data.get(group);

        groupData.put(key, value);
    }

    /**
     * Add a key/value pair to the default group
     */
    public void add(String key, String value) {
        add(defaultGroup, key, value);
    }

    public boolean containsGroup(String group) {
        return data.containsKey(group);
    }

    public boolean containsKey(String key) {
        return containsKey(defaultGroup, key);
    }

    public boolean containsKey(String group, String key) {
        if (!data.containsKey(group)) {
            return false;
        }
        return data.get(group).containsKey(key);
    }

    public String get(String group, String key) {
        if (data.containsKey(group)) {
            Map<String, String> groupData = data.get(group);
            if (groupData.containsKey(key)) {
                return groupData.get(key);
            }
        }
        return null;
    }

    public String get(String key) {
        return get(defaultGroup, key);
    }

    protected String[] getAsList(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        String[] directories = value.split(",");
        // TODO check for empty elements and remove them
        return directories;
    }

    public void remove(String key) {
        remove(defaultGroup, key);
    }

    public void remove(String group, String key) {
        if (!data.containsKey(group)) {
            throw new IllegalArgumentException();
        }

        if (!data.get(group).containsKey(key)) {
            throw new IllegalArgumentException();
        }

        data.get(group).remove(key);
    }

    public Set<String> getGroupNames() {
        return new TreeSet<>(data.keySet());
    }

    public Map<String, String> getGroup(String group) {
        if (!data.containsKey(group)) {
            return null;
        }

        return new HashMap<>(data.get(group));
    }

    public void removeGroup(String groupName) {
        if (!data.containsKey(groupName)) {
            throw new IllegalArgumentException();
        }

        data.remove(groupName);
    }

    protected void checkAllValid() {
        for (Entry<String, Map<String, String>> group : data.entrySet()) {
            String groupName = group.getKey();
            checkValidGroupName(groupName);
            for (Entry<String, String> entry : group.getValue().entrySet()) {
                checkValidKeyValue(groupName, entry.getKey(), entry.getValue());
            }
        }
    }

    protected void checkValidGroupName(String group) {
        if (group == null) {
            throw new IllegalArgumentException("Group name is null");
        }
        if (group.trim().length() == 0) {
            throw new IllegalArgumentException("Group name is empty");
        }
        if (group.contains("[") || group.contains("]")) {
            throw new IllegalArgumentException("Group name contains invalid character");
        }
        if (group.contains("\n")) {
            throw new IllegalArgumentException("Group name contains a newline");
        }
    }

    protected void checkValidKeyValue(String group, String key, String value) {
        if (key == null || key.contains("\n") || key.contains("=") || key.trim().length() == 0) {
            throw new IllegalArgumentException();
        }
        if (value == null || value.contains("\n")) {
            throw new IllegalArgumentException();
        }
        if (knownTypes.containsKey(group)) {
            Map<String, ValueType> knownGroup = knownTypes.get(group);
            ValueType valueType = knownGroup == null ? null : knownGroup.get(key);
            if (valueType == ValueType.LOCALE_STRING) {
                // pass
                // FIXME newlines?
            } else {
                if (key.contains("[") || key.contains("]")) {
                    throw new IllegalArgumentException("non-locale key contains '[' or ']'");
                }
            }

            if (valueType == ValueType.BOOLEAN) {
                if (!isValidBoolean(value)) {
                    throw new IllegalArgumentException("boolean is not true or false");
                }
            }

            if (valueType == ValueType.INTEGER) {
                if (!isValidNumber(value)) {
                    throw new IllegalArgumentException("type numeric is not a valid number");
                }
            }

        }

    }

    protected boolean isValidBoolean(String value) {
        return value.toLowerCase().equals("true") || value.toLowerCase().equals("false");
    }

    protected boolean isValidNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException notDouble) {
            return false;
        }

    }

    public void addKnownType(String group, String key, ValueType valueType) {
        Map<String, ValueType> groupType = knownTypes.get(group);
        if (groupType == null) {
            groupType = new HashMap<>();
            knownTypes.put(group, groupType);
        }
        groupType.put(key, valueType);
    }
}
