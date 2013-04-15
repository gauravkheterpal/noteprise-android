package com.metacube.noteprise.util;

import java.util.Comparator;

import com.evernote.edam.type.Note;

public class CustomComparator implements Comparator<Note>
	{
		
		@Override
		public int compare(Note firstNote, Note secondNote) {		// to sort according to title of a note
		
			if(firstNote.getTitle().compareToIgnoreCase(secondNote.getTitle())>0)
				return +1;
				else if(firstNote.getTitle().compareToIgnoreCase(secondNote.getTitle())<0)
					return -1;
				else
			return 0;
		}

	}
