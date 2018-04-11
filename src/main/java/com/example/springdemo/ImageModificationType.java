package com.example.springdemo;
/**
 * Supported image modification types
 * 
 * @author B
 *
 */
public enum ImageModificationType {
	  Thumbnail("thumbnail", 10, 10);
	  //To add more;
	 
	  private final String name;
	  private final int heigth;
	  private final int width;
	  
	  private ImageModificationType(String name, int width, int heigth) {
	    this.name = name;
	    this.width = width;
	    this.heigth = heigth;
	  }
	 
	  public String getName() {
	    return name;
	  }
	  
	  public int getWidth() {
		  return width;
	  }
	 
	  public int getHeigth() {
	    return heigth;
	  }

	/**
	 * Convert a given modificationType to its relevant enum  
	 * @param modificationType
	 * 			name used to find the enum
	 * @return
	 * 			enum with the name equals to the passed modificationType
	 * 			null if no enum found with that name  
	 */
	public static ImageModificationType get(String modificationType) {
		if (Thumbnail.getName().equals(modificationType)) {
			return Thumbnail;
		}
		return null;
	}
}