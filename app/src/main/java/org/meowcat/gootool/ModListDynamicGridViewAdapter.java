/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.goofans.gootool.addins.Addin;

import org.askerov.dynamicgrid.BaseDynamicGridAdapter;
import org.askerov.dynamicgrid.DynamicGridView;

import java.util.ArrayList;

public class ModListDynamicGridViewAdapter extends BaseDynamicGridAdapter {
    private boolean removeMode = false;

    ModListDynamicGridViewAdapter(Context context, DynamicGridView dgv) {
        super(context, new ArrayList<>(), 1);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ModViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_item, null);
            holder = new ModViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ModViewHolder) convertView.getTag();
        }
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (isRemoveMode() && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)) {

                    TextView titleText = v.findViewById(R.id.mod_item_title);
                    CheckBox enabled = v.findViewById(R.id.mod_item_enabled);
                    ModListDynamicGridViewAdapter.GoomodEntry entry = (ModListDynamicGridViewAdapter.GoomodEntry) getItem(position);
                    entry.setToRemove(!entry.isToRemove());
                    boolean newrm = entry.isToRemove();
                    titleText.setTextColor(newrm ? Color.RED : enabled.isEnabled() ? Color.BLACK : Color.GRAY);
                    return true;
                }
                return false;
            }
        });
        holder.build((GoomodEntry) getItem(position));
        return convertView;
    }

    public void startRemoveMode() {
        this.removeMode = true;
    }

    public boolean isRemoveMode() {
        return removeMode;
    }

    public void onEndRemoveMode() {
        for (int i = getCount() - 1; i >= 0; i--) {
            GoomodEntry e = (GoomodEntry) getItem(i);
            if (e.isToRemove()) {
                remove(e);
                e.getAddin().getDiskFile().delete();
            }
        }
        removeMode = false;
    }

    private class ModViewHolder {
        private TextView titleText;
        private CheckBox enabled;

        private ModViewHolder(View view) {
            titleText = view.findViewById(R.id.mod_item_title);
            enabled = view.findViewById(R.id.mod_item_enabled);
        }

        void build(final GoomodEntry entry) {
            titleText.setText(entry.getName());
            titleText.setTextColor(entry.isEnabled() ? Color.BLACK : Color.GRAY);
            enabled.setChecked(entry.isEnabled());
            enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    entry.enabled = isChecked;
                    titleText.setTextColor(entry.isEnabled() ? Color.BLACK : Color.GRAY);
                }
            });
        }
    }

    public static class GoomodEntry {
        private final Addin addin;
        private boolean enabled;
        private boolean toRemove;

        GoomodEntry(Addin addin, boolean enabled) {
            this.addin = addin;
            this.enabled = enabled;
        }

        public String getName() {
            return addin.getName();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getId() {
            return addin.getId();
        }

        public Addin getAddin() {
            return addin;
        }

        public boolean isToRemove() {
            return toRemove;
        }

        public void setToRemove(boolean toRemove) {
            this.toRemove = toRemove;
        }
    }
}
