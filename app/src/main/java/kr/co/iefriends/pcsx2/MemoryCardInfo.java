package kr.co.iefriends.pcsx2;

import android.content.Context;
import java.lang.Comparable;

public class MemoryCardInfo implements Comparable<MemoryCardInfo> {
    // Constants to define file types
    public static final int FILE_TYPE_UNKNOWN = 0;
    public static final int FILE_TYPE_PS2_8MB = 1;
    public static final int FILE_TYPE_PS2_16MB = 2;
    public static final int FILE_TYPE_PS2_32MB = 3;
    public static final int FILE_TYPE_PS2_64MB = 4;
    public static final int FILE_TYPE_PS1 = 5;

    // Constants to define item types
    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_FOLDER = 2;

    // Variable names
    private final String name;
    private final String path;
    private final int type;
    private final int fileType;
    private final int size;

    // Constructor
    public MemoryCardInfo(String name, String path, int type, int fileType, int size) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.fileType = fileType;
        this.size = size;
    }

    // Method to get a description of the memory card
    public String getDescription(Context context) {
        return String.format("%s (%s)", this.name, getTypeDescription(context));
    }

    // Getter methods
    public int getFileType() {
        return this.fileType;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public int getSize() {
        return this.size;
    }

    public int getType() {
        return this.type;
    }

    // Method to get a description of the type
    public String getTypeDescription(Context context) {
        switch (this.type) {
            case TYPE_FILE:
                switch (this.fileType) {
                    case FILE_TYPE_PS2_8MB:
                        return "PS2/8MB";
                    case FILE_TYPE_PS2_16MB:
                        return "PS2/16MB";
                    case FILE_TYPE_PS2_32MB:
                        return "PS2/32MB";
                    case FILE_TYPE_PS2_64MB:
                        return "PS2/64MB";
                    case FILE_TYPE_PS1:
                        return "PS1/128KB";
                    default:
                        return context.getString(R.string.memory_card_unknown_file); // Unknown file type
                }
            case TYPE_FOLDER:
                return context.getString(R.string.memory_card_type_folder); // Folder description
            case TYPE_EMPTY:
            default:
                return context.getString(R.string.memory_card_empty_slot); // Empty or unknown type
        }
    }

    // Method to compare memory card info objects
    @Override
    public int compareTo(MemoryCardInfo other) {
        if (other == null) {
            return 1;
        }
        return this.name.compareToIgnoreCase(other.name);
    }
}