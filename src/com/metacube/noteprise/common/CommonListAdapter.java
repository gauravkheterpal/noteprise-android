package com.metacube.noteprise.common;

import java.util.ArrayList;
import java.util.Collections;

import android.R.string;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.metacube.noteprise.R;
import com.metacube.noteprise.core.screens.MainMenuScreen;
import com.metacube.noteprise.util.CommonListComparator;
import com.metacube.noteprise.util.NotepriseLogger;
import com.metacube.noteprise.util.imageloader.ImageLoader;

public class CommonListAdapter extends BaseAdapter implements OnClickListener
{
	public ArrayList<CommonListItems> listItems = null;
	int count;
	LayoutInflater inflater = null;
	View listItemLayout = null;
	
	TextView listItemMainTextView = null,listItemMainTextsize;
	public	ImageView leftImageView = null, listArrowImageView = null, listItemCheckBox = null, listSectionUpButton;
	Boolean isCheckListMode = Boolean.FALSE;
	Boolean isInnerListMode = Boolean.FALSE;
	BaseFragment baseFragment;

	public CommonListAdapter(LayoutInflater inflater, ArrayList<CommonListItems> listItems) 
	{
		this.listItems = listItems;
		this.count = listItems.size();
		this.inflater = inflater;
	}
	
	public CommonListAdapter(BaseFragment baseFragment, LayoutInflater inflater, ArrayList<CommonListItems> listItems) 
	{
		this.listItems = listItems;
		this.count = listItems.size();
		this.inflater = inflater;
		this.baseFragment = baseFragment;
	}
	public void clearCheckedItem()
	 {
	  for (int i = 0; i < listItems.size(); i++)
	   listItems.get(i).setIsChecked(false);
	 }
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		CommonListItems item = listItems.get(position);
		if (item.getItemType().equalsIgnoreCase(Constants.ITEM_TYPE_LIST_SECTION))
		{
			listItemLayout = inflater.inflate(R.layout.list_section_layout, parent, false);
			listItemMainTextView = (TextView) listItemLayout.findViewById(R.id.list_section_item_text_view);
			String sectionTitle = item.getLabel();
			if (item.getTotalContent() != null)
			{
				sectionTitle = sectionTitle + "   (" + item.getTotalContent() + ")";
			}
			if (isInnerListMode)
			{
				listSectionUpButton = (ImageView) listItemLayout.findViewById(R.id.list_section_item_up_image);
				listSectionUpButton.setVisibility(View.VISIBLE);
				listSectionUpButton.setOnClickListener(this);
			}
		
				
			listItemMainTextView.setText(sectionTitle);			
		}
		else if(item.getItemType().equalsIgnoreCase(Constants.ITEM_TYPE_LIST_ATTACHMENT)){
			listItemLayout = inflater.inflate(R.layout.common_list_item_layout, parent, false);
			listItemMainTextView = (TextView) listItemLayout.findViewById(R.id.list_item_main_text);
			listItemMainTextsize =(TextView) listItemLayout.findViewById(R.id.list_item_size); 
			leftImageView = (ImageView) listItemLayout.findViewById(R.id.list_item_left_image);
			listItemCheckBox = (ImageView) listItemLayout.findViewById(R.id.list_item_checkbox_image);
			listArrowImageView = (ImageView) listItemLayout.findViewById(R.id.list_item_arrow_image);
			listItemMainTextView.setText(item.getLabel());			
			if(item.getAttachmentLength()!=null && listItemMainTextsize !=null)
				listItemMainTextsize.setText(item.getAttachmentLength()+""+"KB");
			if (isCheckListMode)
			{
				if (item.getIsChecked())
				{
					listItemCheckBox.setImageResource(R.drawable.button_checked);
				}
				else
				{
					listItemCheckBox.setImageResource(R.drawable.button_unchecked);
				}			
				listItemCheckBox.setVisibility(View.VISIBLE);
			}
			if (item.getLeftUserImageURL() != null)
			{
				leftImageView.setVisibility(View.VISIBLE);
				if (baseFragment != null)
				{
					baseFragment.loadImageOnView(item.getLeftUserImageURL(), leftImageView, ImageLoader.UNCOMPRESSED);
				}
			}
			else if (item.getLeftImage() != null)
			{
				leftImageView.setImageResource(item.getLeftImage());
				leftImageView.setVisibility(View.VISIBLE);
			}
			if (item.getShowListArrow() && !isCheckListMode)
			{
				listArrowImageView.setVisibility(View.VISIBLE);
			}
			
			if (item.getAttachmentLength() != null)
			{
				listItemMainTextsize.setVisibility(View.VISIBLE);
			}
		}
		else
		{
			listItemLayout = inflater.inflate(R.layout.common_list_item_layout, parent, false);
			listItemMainTextView = (TextView) listItemLayout.findViewById(R.id.list_item_main_text);
			listItemMainTextsize =(TextView) listItemLayout.findViewById(R.id.list_item_size); 
			leftImageView = (ImageView) listItemLayout.findViewById(R.id.list_item_left_image);
			listItemCheckBox = (ImageView) listItemLayout.findViewById(R.id.list_item_checkbox_image);
			listArrowImageView = (ImageView) listItemLayout.findViewById(R.id.list_item_arrow_image);
			listItemMainTextView.setText(item.getLabel());			
			if(item.getAttachmentLength()!=null && listItemMainTextsize !=null)
				listItemMainTextsize.setText(item.getAttachmentLength()+""+"KB");
			if (isCheckListMode)
			{
				if (item.getIsChecked())
				{
					listItemCheckBox.setImageResource(R.drawable.button_checked);
				}
				else
				{
					listItemCheckBox.setImageResource(R.drawable.button_unchecked);
				}			
				listItemCheckBox.setVisibility(View.VISIBLE);
			}
	
		
			if (item.getLeftUserImageURL() != null)
			{
				leftImageView.setVisibility(View.VISIBLE);
				if (baseFragment != null)
				{
					baseFragment.loadImageOnView(item.getLeftUserImageURL(), leftImageView, ImageLoader.UNCOMPRESSED);
				}
			}
			else if (item.getLeftImage() != null)
			{
				leftImageView.setImageResource(item.getLeftImage());
				leftImageView.setVisibility(View.VISIBLE);
			}
			if (item.getShowListArrow() && !isCheckListMode)
			{
				listArrowImageView.setVisibility(View.VISIBLE);
			}
			
			if (item.getAttachmentLength() != null)
			{
				listItemMainTextsize.setVisibility(View.VISIBLE);
			}
		}		
		return listItemLayout;
	}

	@Override
	public int getCount() 
	{
		this.count = listItems.size();
		return count;
	}
	
	public String getListItemText(int position)
	{
		return listItems.get(position).getName();
	}
	
	public String getListItemId(int position)
	{
		return listItems.get(position).getId();
	}
	
	public String getSortData(int position)
	{
		return listItems.get(position).getSortData();
	}
	
	public String getTag(int position)
	{
		return listItems.get(position).getTag();
	}

	@Override
	public Object getItem(int position) 
	{
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}
	
	public Boolean isListItem(int position)
	{
		if (listItems.get(position).getItemType().equalsIgnoreCase(Constants.ITEM_TYPE_LIST_SECTION))
		{
			return false;
		}
		return true;
	}

	
	public void isInnerList(Boolean status)
	{
		isInnerListMode = status;
	}
	
	public void showCheckList()
	{
		isCheckListMode = Boolean.TRUE;
		notifyDataSetChanged();
	}
	public void hideCheckList()
	{
		isCheckListMode = Boolean.FALSE;
		notifyDataSetChanged();
	}
	public Boolean isItemChecked(int position)
	{
		return listItems.get(position).getIsChecked();
	}
	
	public void setChecedkCurrentItem(int position)
	{
		
		NotepriseLogger.logMessage("In check");
		if (isItemChecked(position))
		{
			listItems.get(position).setIsChecked(Boolean.FALSE);
		}
		else
		{
			listItems.get(position).setIsChecked(Boolean.TRUE);
		}		
		notifyDataSetChanged();
	}
	public void setUnChecedkItem(int position)
	{				
		listItems.get(position).setIsChecked(Boolean.FALSE);			
		notifyDataSetChanged();
	}
	
	public Boolean isCheckListMode()
	{
		return isCheckListMode;
	}
	
	public ArrayList<String> getCheckedItemsList()
	{
		ArrayList<String> checkedList = new ArrayList<String>();
		for (int i = 0; i < listItems.size(); i++)
		{
			if(listItems.get(i).getIsChecked())
			{
				checkedList.add(listItems.get(i).getId());
				
			}
		}
		return checkedList;
	}
	
	public String getCheckedItemsListName()
	{
		String checkedList = null;
		for (int i = 0; i < listItems.size(); i++)
		{
			if(listItems.get(i).getIsChecked())
			{
				checkedList=listItems.get(i).getLabel();
				
			}
		}
		return checkedList;
	}
	
	public int getCheckedItemsUserNameLength()
	{
		int length = 0;
		for (int i = 0; i < listItems.size(); i++)
		{
			if(listItems.get(i).getIsChecked())
			{				
				length += listItems.get(i).getLabel().length() + 1;
			}
		}
		return length;
	}
	public void clearCheckedItems(ArrayList<CommonListItems> listItems)//to clear checked items
	{
		for (int i = 0; i < listItems.size(); i++)
		{
			if(listItems.get(i).getIsChecked())
			{				
				listItems.get(i).setIsChecked(false);
			}
		}
	}
	public void changeOrdering(String orderType)
	{
		// Sort By Name
		if(orderType.equalsIgnoreCase(Constants.SORT_BY_NAME))
		{
			Collections.sort(listItems, new CommonListComparator(CommonListComparator.COMPARE_BY_NAME));
		}
		// Sort By Sort Order
		else if(orderType.equalsIgnoreCase(Constants.SORT_BY_SORT_ORDER))
		{
			Collections.sort(listItems, new CommonListComparator(CommonListComparator.COMPARE_BY_SORT_DATA));
		}
		// Sort By id
		else if (orderType.equalsIgnoreCase(Constants.SORT_BY_ID))
		{
			Collections.sort(listItems, new CommonListComparator(CommonListComparator.COMPARE_BY_ID));
		}
		// By default sort by Label
		else if (orderType.equalsIgnoreCase(Constants.SORT_BY_LABEL))
		{
			Collections.sort(listItems, new CommonListComparator(CommonListComparator.COMPARE_BY_LABEL));
		}
		notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) 
	{
		((MainMenuScreen)baseFragment).loadPreviousList(false);	
	}
}