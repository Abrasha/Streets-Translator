package aabrasha.ua.streettranslator.view.adapter;

import aabrasha.ua.streettranslator.R;
import aabrasha.ua.streettranslator.model.StreetEntry;
import aabrasha.ua.streettranslator.util.StringHighlightUtils;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static aabrasha.ua.streettranslator.util.StringUtils.EMPTY_STRING;

/**
 * @author Andrii Abramov on 8/28/16.
 */
public class StreetEntryViewHolder extends RecyclerView.ViewHolder {

    private TextView tvOldName;
    private TextView tvNewName;
    private TextView tvDescription;

    private ImageView ivArrow;

    private String pattern;

    public StreetEntryViewHolder(View container) {
        super(container);
        findViews(container);
    }

    private void findViews(View container) {
        tvNewName = (TextView) container.findViewById(R.id.item_text_new_street_name);
        tvOldName = (TextView) container.findViewById(R.id.item_text_old_street_name);
        tvDescription = (TextView) container.findViewById(R.id.item_text_street_description);

        ivArrow = (ImageView) container.findViewById(R.id.iv_arrow);
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setStreetItem(StreetEntry item) {
        setOldName(item.getOldName());
        setNewName(item.getNewName());
        setDescription(item.getDescription());
    }

    private void setOldName(String oldName) {
        tvOldName.setText(fillInPattern(oldName));
    }

    private void setNewName(String newName) {
        if (newName == null) {
            onEmptyNewName();
        } else {
            fillInNewName(newName);
        }
    }

    private void onEmptyNewName() {
        tvNewName.setText(EMPTY_STRING);
        tvNewName.setVisibility(View.GONE);
        ivArrow.setVisibility(View.GONE);
    }

    private void fillInNewName(String newName) {
        tvNewName.setText(fillInPattern(newName));
        tvNewName.setVisibility(View.VISIBLE);
        ivArrow.setVisibility(View.VISIBLE);
    }

    private void setDescription(String description) {
        if (hasDescription(description)) {
            tvDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(description);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
    }

    private boolean hasDescription(String description) {
        return description != null && !description.isEmpty();
    }

    private Spannable fillInPattern(String s) {
        return StringHighlightUtils.highlight(s, pattern);
    }
}