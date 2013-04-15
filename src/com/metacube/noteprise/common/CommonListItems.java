package com.metacube.noteprise.common;

public class CommonListItems 
{
	private String id = null;
	private String name = null;
	private String label = null;
	private String sortData = null;
	private String tag = null;
	private Boolean isChecked = Boolean.FALSE;
	private Boolean isSelected = Boolean.FALSE;
	public Boolean getIsSelected() {
		return isSelected;
	}
	public void setIsSelected(Boolean isSelected) {
		this.isSelected = isSelected;
		
	}
	private Integer leftImage = null;
	private String leftUserImageURL = null;
	private String itemType = Constants.ITEM_TYPE_LIST_ITEM;
	private String listItemType = Constants.LIST_ITEM_TYPE_NOTE;
	private Integer totalContent = null;
	private Integer fieldLength = null;
	private String attachmentLength=null;
	private Boolean showListArrow = Boolean.TRUE;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getSortData() {
		return sortData;
	}
	public void setSortData(String sortData) {
		this.sortData = sortData;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Boolean getIsChecked() {
		return isChecked;
	}
	public void setIsChecked(Boolean isChecked) {
		this.isChecked = isChecked;
	}
	public Integer getLeftImage() {
		return leftImage;
	}
	public void setLeftImage(Integer leftImage) {
		this.leftImage = leftImage;
	}
	public String getLeftUserImageURL() {
		return leftUserImageURL;
	}
	public void setLeftUserImageURL(String leftUserImageURL) {
		this.leftUserImageURL = leftUserImageURL;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public String getListItemType() {
		return listItemType;
	}
	public void setListItemType(String listItemType) {
		this.listItemType = listItemType;
	}
	public Integer getTotalContent() {
		return totalContent;
	}
	public void setTotalContent(Integer totalContent) {
		this.totalContent = totalContent;
	}
	public Integer getFieldLength() {
		return fieldLength;
	}
	public void setFieldLength(Integer fieldLength) {
		this.fieldLength = fieldLength;
	}
	public String getAttachmentLength() {
		return attachmentLength;
	}
	public void setAttachmentLength(String attachmentLength) {
		this.attachmentLength = attachmentLength;
	}
	public Boolean getShowListArrow() {
		return showListArrow;
	}
	public void setShowListArrow(Boolean showListArrow) {
		this.showListArrow = showListArrow;
	}
	

}