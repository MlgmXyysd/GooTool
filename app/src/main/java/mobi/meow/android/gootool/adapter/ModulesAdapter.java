package mobi.meow.android.gootool.adapter;

import static mobi.meow.android.gootool.MeowCatApplication.context;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.wog.WorldOfGoo;

import java.util.ArrayList;

import mobi.meow.android.gootool.R;

public class ModulesAdapter extends RecyclerView.Adapter<ModulesAdapter.ModulesViewHolder> {
    private final Context mContext;
    public ArrayList<Object> list = new ArrayList<>();
    private boolean removeMode = false;

    public ModulesAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public int getCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void add(Object item) {
        list.add(item);
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public void startRemoveMode() {
        this.removeMode = true;
    }

    public boolean isRemoveMode() {
        return removeMode;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("NotifyDataSetChanged")
    public void onEndRemoveMode() {
        for (int i = list.size() - 1; i >= 0; i--) {
            GoomodEntry e = (GoomodEntry) list.get(i);
            if (e.isEnabled()) {
                WorldOfGoo.availableAddins.remove(e.getAddin());
                list.remove(i);
                notifyDataSetChanged();
                e.getAddin().getDiskFile().delete();
            }
        }
        removeMode = false;
    }

    @NonNull
    @Override
    public ModulesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ModulesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.mod_item, viewGroup, false));
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    public void onBindViewHolder(ModulesViewHolder viewHolder, int position) {
        View convertView = viewHolder.mView;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mod_item, null);
            viewHolder = new ModulesViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        convertView.setOnTouchListener((v, event) -> {
            if (isRemoveMode() && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)) {
                GoomodEntry entry = (GoomodEntry) list.get(position);
                entry.setToRemove(!entry.isToRemove());
                return true;
            }
            return false;
        });
        viewHolder.build((GoomodEntry) list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ModulesViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText, authorText, descriptionText, versionText, packageText, typeText;
        private final CheckBox enabled;
        private final View mView;

        public ModulesViewHolder(View view) {
            super(view);
            mView = view;
            titleText = view.findViewById(R.id.mod_item_title);
            authorText = view.findViewById(R.id.mod_author_name);
            descriptionText = view.findViewById(R.id.mod_description);
            versionText = view.findViewById(R.id.mod_version);
            packageText = view.findViewById(R.id.mod_package_name);
            typeText = view.findViewById(R.id.mod_type);
            enabled = view.findViewById(R.id.mod_item_enabled);
        }

        @SuppressLint("SetTextI18n")
        void build(final GoomodEntry entry) {
            titleText.setText(entry.getName());
            authorText.setText("@" + entry.getAuthor());
            descriptionText.setText(entry.getDescription());
            versionText.setText(entry.getVersion());
            packageText.setText(entry.getId());
            typeText.setText(entry.getType());
            enabled.setChecked(entry.isEnabled());
            enabled.setOnCheckedChangeListener((buttonView, isChecked) -> entry.enabled = isChecked);
        }
    }

    public static class GoomodEntry {
        private final Addin addin;
        private boolean enabled;
        private boolean toRemove;

        public GoomodEntry(Addin addin, boolean enabled) {
            this.addin = addin;
            this.enabled = enabled;
        }

        public String getName() {
            return addin.getName();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getAuthor() {
            return addin.getAuthor();
        }

        public String getId() {
            return addin.getId();
        }

        public String getVersion() {
            return addin.getVersion().toString();
        }

        public String getType() {
            return addin.getTypeText();
        }

        public String getDescription() {
            return addin.getDescription();
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
