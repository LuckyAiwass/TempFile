package com.ubx.appinstall.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.ubx.appinstall.R;

public class OpenFileDialog {
	public static String tag = "OpenFileDialog";
	static final public String sRoot = "/";
	static final public String sParent = "..";
	static final private String sOnErrorMsg = "No rights to access!";

	// 参数说明
	// context:上下文
	// dialogid:对话框ID
	// title:对话框标题
	// callback:一个传递Bundle参数的回调接口
	// suffix:需要选择的文件后缀，比如需要选择wav、mp3文件的时候设置为".wav;.mp3;"，注意最后需要一个分号(;)
	// images:用来根据后缀显示的图标资源ID。
	// 根目录图标的索引为sRoot;
	// 父目录的索引为sParent;
	// 文件夹的索引为sFolder;
	// 默认图标的索引为sEmpty;
	// 其他的直接根据后缀进行索引，比如.wav文件图标的索引为"wav"
	public static Dialog createDialog(int id, Context context, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectView(context, id));
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

				// add SharedPrefs
				arg0.dismiss();
			}

		});
		Dialog dialog = builder.create();
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(title);
		return dialog;
	}

	
	static class FileSelectView extends RecyclerView {

		private CallbackBundle callback = null;
		private List<Map<String, Object>> list = new ArrayList<>();
		private FileAdapter adapter;
		private static final int VOLUME_SDCARD_INDEX = 1;
		private static final int VOLUME_UDIST_INDEX = 2;

		public FileSelectView(Context context, int dialogid) {
			super(context);
			adapter = new FileAdapter(getContext(), list);
			this.setAdapter(adapter);
			refreshFileList(sRoot);
			setLayoutManager(new LinearLayoutManager(context));
		}

		private int refreshFileList(String mPath) {
			list.clear();
			// 刷新文件列表
			File[] files = null;
			try {
				files = new File(mPath).listFiles();
			} catch (Exception e) {
				files = null;
			}
			if (files == null) {
				// 访问出错
				Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT).show();
				return -1;
			}

			// 用来先保存文件夹列表
			ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();

			if (!mPath.equals(sRoot)) {
				// 添加上一层目录
				Map<String, Object> upmap = new HashMap<String, Object>();
				upmap = new HashMap<String, Object>();
				upmap.put("name", sParent);
				upmap.put("path", mPath);
				upmap.put("isApk",false);
				list.add(upmap);

				List<String> selectedFile = SharedPrefsStrListUtil.getStrListValue(getContext(), "ADD_DIR");
				List<String> selectedAPK = SharedPrefsStrListUtil.getStrListValue(getContext(), "ADD_APK");
				for (File file : files) {
					if (file.isDirectory() && file.listFiles() != null) {
						// 添加文件夹
						if (!file.getName().startsWith(".")) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("name", file.getName());
							map.put("path", file.getPath());
							map.put("isApk",false);
							for (String path : selectedFile) {
								if (file.getPath().equals(path)) {
									map.put("enabled", true);
								}
							}
							lfolders.add(map);
						}
					}
				}
				for (File file : files){
					if (file.isFile() && file.getAbsolutePath().toLowerCase().endsWith(".apk")) {
						PackageManager pm = getContext().getPackageManager();
					        PackageInfo info = pm.getPackageArchiveInfo( file.getPath(),0);
					        if(info!=null){
						    Map<String, Object> map = new HashMap<String, Object>();
						    map.put("name", file.getName());
						    map.put("path", file.getPath());
						    map.put("isApk",true);
						    for (String path : selectedAPK) {
						    	if (file.getPath().equals(path)) {
						    		map.put("enabled", true);
						    	}
						    }
						    lfolders.add(map);
						}
					}
				}
			} else {
				// 添加 内部存储 和sd卡
				List<String> selectedFile = SharedPrefsStrListUtil.getStrListValue(getContext(), "ADD_DIR");
				Map<String, Object> map = new HashMap<String, Object>();
				map = new HashMap<String, Object>();
				map.put("name",getContext().getString(R.string.shared_storage));
				map.put("path", "/storage/emulated/0/");
				map.put("isApk",false);
				list.add(map);
				
				for (String path : selectedFile) {
					if (path.equals("/storage/emulated/0/")) {
						map.put("enabled", true);
					}
				}

				if (!getSDPath(getContext()).equals("")) {
					Map<String, Object> map1 = new HashMap<String, Object>();
					map1 = new HashMap<String, Object>();
					map1.put("name", getContext().getString(R.string.sd_card));
					map1.put("path", getSDPath(getContext()));
					map1.put("isApk",false);
					list.add(map1);

					for (String path : selectedFile) {
						if (path.equals(getSDPath(getContext()))) {
							map1.put("enabled", true);
						}
					}
				}
				if (!getUdistPath(getContext()).equals("")) {
					Map<String, Object> map1 = new HashMap<String, Object>();
					map1 = new HashMap<String, Object>();
					map1.put("name", getContext().getString(R.string.u_dist));
					map1.put("path", getUdistPath(getContext()));
					map1.put("isApk",false);
					list.add(map1);

					for (String path : selectedFile) {
						if (path.equals(getUdistPath(getContext()))) {
							map1.put("enabled", true);
						}
					}
				}
			}

			list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
			adapter.notifyDataSetChanged();

			// this.getChildViewHolder().itemView.setOnClickListener();
			return files.length;
		}

		public String getSDPath(Context mcon) {
			String sd = null;
			StorageManager mStorageManager = (StorageManager) mcon.getSystemService(Context.STORAGE_SERVICE);
			StorageVolume[] volumes = mStorageManager.getVolumeList();
			for(StorageVolume volume:volumes){
				ULog.d(volume.getPath());
			}
			StorageVolume mVolume = (volumes.length > VOLUME_SDCARD_INDEX) ? volumes[VOLUME_SDCARD_INDEX] : null;
			if (mVolume != null) {
				sd = volumes[VOLUME_SDCARD_INDEX].getPath();
				return sd;
			}
			return "";
		}
		public String getUdistPath(Context mcon) {
			String ud = null;
			StorageManager mStorageManager = (StorageManager) mcon.getSystemService(Context.STORAGE_SERVICE);
			List<VolumeInfo> volumes = mStorageManager.getVolumes();
			for(VolumeInfo volumeInfo:volumes){
				//ULog.d(volume.getPath());
			
			//StorageVolume mVolume = (volumes.length > VOLUME_UDIST_INDEX) ? volumes[VOLUME_UDIST_INDEX] : null;
			//if (mVolume != null) {
			//	ud = volumes[VOLUME_UDIST_INDEX].getPath();
			//	return ud;
			//}
                            if(volumeInfo.getType() == 0){
                                DiskInfo distInfo = volumeInfo.getDisk();
                                if(distInfo!=null&&distInfo.isUsb()){
                                    return volumeInfo.getPath().getPath();
                                }
                            }
                        }
			return "";
		}

		class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
			private Context context;
			private CallbackBundle mCallback = null;
			private List<Map<String, Object>> list = null;
			private String path;

			public FileAdapter(Context context, List<Map<String, Object>> list) {
				this.context = context;
				this.mCallback = callback;
				this.list = list;

			}

			@Override
			public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				View view = LayoutInflater.from(context).inflate(R.layout.filedialog_item, null, false);
				final FileViewHolder viewHolder = new FileViewHolder(view);
				viewHolder.itemView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						int position = viewHolder.getAdapterPosition();
						String pt = (String) list.get(position).get("path");
						String fn = (String) list.get(position).get("name");
						if (fn.equals(sParent)) {
							// 如果是更目录或者上一层
							File fl = new File(pt);
							String ppt = fl.getParent();
							if (ppt != null) {
								// 返回上一层
								path = ppt;
							}
							if (pt.equals("/storage/emulated/0/") || pt.equals(getSDPath(context))) {
								path = sRoot;
							}
						} else {
							File fl = new File(pt);
							// 进入选中的文件夹
							path = pt;
						}
						if ((boolean) list.get(position).get("isApk"))
							viewHolder.enabled.setChecked(!viewHolder.enabled.isChecked());
						else
						    refreshFileList(path);
					}
				});
				viewHolder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
						int position = viewHolder.getAdapterPosition();
						list.get(position).put("enabled", b);
						if (b) {
							if ((boolean)list.get(position).get("isApk"))
								SharedPrefsStrListUtil.addStrListValue(context, "ADD_APK",
										list.get(position).get("path").toString());
							else
								SharedPrefsStrListUtil.addStrListValue(context, "ADD_DIR",
									list.get(position).get("path").toString());

						} else {
							if ((boolean)list.get(position).get("isApk"))
								SharedPrefsStrListUtil.removeStrListItem(context, "ADD_APK",
										list.get(position).get("path").toString());
							else
								SharedPrefsStrListUtil.removeStrListItem(context, "ADD_DIR",
									list.get(position).get("path").toString());
						}
					}
				});
				return viewHolder;
			}

			@Override
			public void onBindViewHolder(FileViewHolder holder, int position) {
				if ((boolean)list.get(position).get("isApk")){
					PackageManager pm = context.getPackageManager();
					PackageInfo info = pm.getPackageArchiveInfo((String)list.get(position).get("path"),
							0);
				    if(info!=null&&info.applicationInfo!=null){
					info.applicationInfo.sourceDir =(String)list.get(position).get("path");
					info.applicationInfo.publicSourceDir = (String)list.get(position).get("path");
					ApplicationInfo appInfo = info.applicationInfo;
					holder.img.setImageDrawable(appInfo.loadIcon(pm));
					holder.enabled.setVisibility(View.VISIBLE);
					holder.name.setText(appInfo.loadLabel(pm).toString());
			            }else{
					holder.name.setText("Something Error");
			            }
					
				}else {
					if (list.get(position).get("name").equals(sParent)) {
						holder.img.setImageResource(R.drawable.filedialog_folder_up);
						holder.enabled.setVisibility(View.GONE);
					} else {
						holder.img.setImageResource(R.drawable.filedialog_folder);
						holder.enabled.setVisibility(View.VISIBLE);
					}
					holder.name.setText(list.get(position).get("name").toString());
				}
				holder.path.setText(list.get(position).get("path").toString());

				if (list.get(position).get("enabled") != null && (boolean) list.get(position).get("enabled"))
					holder.enabled.setChecked(true);
				else {
					holder.enabled.setChecked(false);
				}
			}

			@Override
			public int getItemCount() {
				return list == null ? 0 : list.size();
			}

			public List<Map<String, Object>> getList() {
				return this.list;
			}

			class FileViewHolder extends RecyclerView.ViewHolder {
				private ImageView img;
				private TextView name;
				private TextView path;
				private CheckBox enabled;

				public FileViewHolder(View itemView) {
					super(itemView);
					img = (ImageView) itemView.findViewById(R.id.item_img);
					name = (TextView) itemView.findViewById(R.id.item_name);
					path = (TextView) itemView.findViewById(R.id.item_name);
					enabled = (CheckBox) itemView.findViewById(R.id.item_check);

				}
			}
		}

	}
}
