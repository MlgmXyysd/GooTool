package mobi.meow.android.gootool;

import static android.app.Activity.RESULT_OK;
import static mobi.meow.android.gootool.MeowCatApplication.TAG;
import static mobi.meow.android.gootool.MeowCatApplication.context;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.wog.WorldOfGoo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import mobi.meow.android.gootool.adapter.ModulesAdapter;

public class ModulesFragment extends Fragment {

    public Button addBtn, rmBtn, buildBtn;
    RecyclerView modsGrid;
    ConstraintLayout list_empty_tip;
    ItemTouchHelper mItemTouchHelper;
    private ProgressBar pb;
    private ModulesAdapter modListAdapter;
    private TextView text;
    private ActivityResultLauncher<Intent> chooseFileLauncher;
    private Intent chooseFileIntent;

    public ModulesFragment() {
        // Required empty public constructor
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chooseFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                new AddAddinAsyncTask(
                        Objects.requireNonNull(result.getData()).getData(),
                        (ModulesAdapter) modsGrid.getAdapter(),
                        requireActivity()
                ).execute((Void[]) null);
            }
        });

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFileIntent = Intent.createChooser(intent, getString(R.string.add));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        modsGrid = view.findViewById(R.id.modsGrid);
        modsGrid.setAdapter(modListAdapter = new ModulesAdapter(context));
        modsGrid.setLayoutManager(manager);
        mItemTouchHelper = new ItemTouchHelper(new ModulesItemTouchHelper(modListAdapter));
        mItemTouchHelper.attachToRecyclerView(modsGrid);
        list_empty_tip = view.findViewById(R.id.list_empty_tip);

        pb = requireActivity().findViewById(R.id.installProgress);
        pb.setInterpolator(new LinearInterpolator());

        text = requireActivity().findViewById(R.id.textView);

        addBtn = requireActivity().findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> {

            try {
                chooseFileLauncher.launch(chooseFileIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), R.string.managernotfound, Toast.LENGTH_SHORT).show();
            }
        });

        rmBtn = requireActivity().findViewById(R.id.rmBtn);
        rmBtn.setOnClickListener(v -> {
            ModulesAdapter adapter = (ModulesAdapter) modsGrid.getAdapter();
            assert adapter != null;
            if (adapter.isRemoveMode()) {
                adapter.onEndRemoveMode();
                ((Button) v).setText(R.string.btn_remove);
                Toast.makeText(getActivity(), R.string.deleted, Toast.LENGTH_LONG).show();
                enableButtons();
            } else {
                adapter.startRemoveMode();
                ((Button) v).setText(R.string.btn_done);
                Toast.makeText(getActivity(), R.string.delete_select, Toast.LENGTH_LONG).show();
                disableButtons();
                v.setEnabled(true);
            }
            setModuleViewListVisible();
        });

        buildBtn = requireActivity().findViewById(R.id.buildBtn);
        buildBtn.setOnClickListener(v -> {
            new GoomodInstaller(this, pb, text, modsGrid);
            new ApkInstaller(this, pb, text);
        });

        new InitGootoolTask().execute();
    }

    public void disableButtons() {
        setButtonsEnabled(false);
    }

    public void enableButtons() {
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean value) {
        rmBtn.setEnabled(value);
        addBtn.setEnabled(value);
        buildBtn.setEnabled(value);
    }

    private void setModuleViewListVisible() {
        boolean value = WorldOfGoo.getAvailableAddins().size() != 0;
        modsGrid.setVisibility(value ? View.VISIBLE : View.GONE);
        list_empty_tip.setVisibility(value ? View.GONE : View.VISIBLE);
    }

    static class ModulesItemTouchHelper extends ItemTouchHelper.Callback {
        private final ModulesAdapter mViewAdapter;
        private int longClickPosition = -1;

        public ModulesItemTouchHelper(ModulesAdapter recycleViewAdapter) {
            Log.d("dddd", "into MyItemTouchHelper");
            this.mViewAdapter = recycleViewAdapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
            mViewAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                longClickPosition = viewHolder.getAdapterPosition();
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (longClickPosition != -1) {
                Collections.swap(mViewAdapter.list, longClickPosition, adapterPosition);
                mViewAdapter.notifyDataSetChanged();
            }
            super.clearView(recyclerView, viewHolder);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }

    @SuppressLint("StaticFieldLeak")
    class AddAddinAsyncTask extends AsyncTask<Void, Void, String> {
        private final Uri fileUri;
        private final ModulesAdapter adapter;
        private final Context context;

        AddAddinAsyncTask(Uri fileUri, ModulesAdapter adapter, Context context) {
            this.fileUri = fileUri;
            this.adapter = adapter;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                File file;
                try {
                    file = File.createTempFile("tmp-", null, context.getCacheDir());
                    Utilities.copyFile(IOUtils.getFile(requireContext(), fileUri), file);
                } catch (Exception e) {
                    file = Utilities.copyUriFileToTemp(fileUri, context);
                }
                Addin a = AddinFactory.loadAddin(file);
                WorldOfGoo.getTheInstance().installAddin(file, a.getId(), false);
            } catch (AddinFormatException e) {
                Log.e(TAG, "Addin error", e);
                return getString(R.string.invaild);
            } catch (IOException e) {
                Log.e(TAG, "IO error", e);
                return getString(R.string.readerror);
            } catch (DuplicateAddinException ex) {
                Log.e(TAG, "Duplicate addin", ex);
                return getString(R.string.added);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                return;
            }
            Set<String> alreadyInstalled = new HashSet<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                alreadyInstalled.add(((ModulesAdapter.GoomodEntry) adapter.getItem(i)).getId());
            }
            for (Addin addin : WorldOfGoo.getAvailableAddins()) {
                if (!alreadyInstalled.contains(addin.getId())) {
                    adapter.add(new ModulesAdapter.GoomodEntry(addin, false));
                }
            }
            setModuleViewListVisible();
            Toast.makeText(context, R.string.goomodadded, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InitGootoolTask extends AsyncTask<Void, ProgressData, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            WorldOfGoo.getTheInstance().init();
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (MeowCatApplication.worldOfGooApp == null) {
                return;
            }
            enableButtons();
            pb.setVisibility(View.INVISIBLE);
            pb.setProgress(0);
            text.setText("");

            try {
                WorldOfGoo wog = WorldOfGoo.getTheInstance();

                wog.updateInstalledAddins();
                Configuration cfg = WorldOfGoo.getTheInstance().readConfiguration();

                for (Addin addin : WorldOfGoo.getAvailableAddins()) {
                    modListAdapter.add(new ModulesAdapter.GoomodEntry(addin, cfg.isEnabledAdddin(addin.getId())));
                }
                setModuleViewListVisible();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
            WoGInitData.setPackageManager(requireActivity().getPackageManager());
            WoGInitData.setContext(requireContext());

            WoGInitData.setProgressListener(new ProgressListener() {
                private String stepName;

                @Override
                public void beginStep(String taskDescription, boolean progressAvailable) {
                    stepName = taskDescription;
                    if (!progressAvailable) {
                        progressStep(0.5f);
                    } else {
                        progressStep(0);
                    }
                }

                @Override
                public void progressStep(float percent) {
                    publishProgress(new ProgressData(stepName, percent));
                }
            });
        }

        @Override
        protected void onProgressUpdate(ProgressData... i) {
            ProgressData pd = i[i.length - 1];
            pb.setProgress((int) (pd.progress * 100));
            text.setText(pd.name);

        }
    }
}