package com.application.smartbiosensor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.application.smartbiosensor.service.CameraService;
import com.application.smartbiosensor.service.ImageProcessingService;

import java.util.ArrayList;

public class CalibrationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private static final int TYPE_FOOTER = 2;
        private ViewHolderItemCalibration viewHolderItem;
        private ViewHolderHeaderCalibration viewHolderHeader;
        private Context context;
        private ArrayList<CalibrationItem> calibrationItems;
        private CameraService cameraService;
        private ImageProcessingService imageProcessingService;
        private ArrayList<RecyclerView.ViewHolder> boundViewHolders;

        public CalibrationAdapter(Context context, ArrayList<CalibrationItem> calibrationItems, CameraService cameraService, ImageProcessingService imageProcessingService) {
            this.calibrationItems = calibrationItems;
            this.context = context;
            this.cameraService = cameraService;
            this.imageProcessingService = imageProcessingService;
            this.boundViewHolders = new ArrayList<RecyclerView.ViewHolder>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == TYPE_ITEM){
                return new ViewHolderItemCalibration(LayoutInflater.from(parent.getContext()), parent, context, cameraService, imageProcessingService);
            }else if(viewType == TYPE_HEADER){
                    return new ViewHolderHeaderCalibration(LayoutInflater.from(parent.getContext()), parent, context, cameraService, imageProcessingService);
            }else if(viewType == TYPE_FOOTER){
                return new ViewHolderFooterCalibration(LayoutInflater.from(parent.getContext()), parent, context);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if(!boundViewHolders.contains(holder))
                boundViewHolders.add(holder);

            if (holder instanceof ViewHolderItemCalibration) {
                viewHolderItem = (ViewHolderItemCalibration)holder;
                viewHolderItem.mediumTitle.setText(R.string.medium_title);
                viewHolderItem.refractiveIndexTitle.setText(R.string.medium_refractive_index_title);
                viewHolderItem.medium.setText(getItem(position).getMedium());
                viewHolderItem.refractiveIndex.setText(String.valueOf(getItem(position).getRefractiveIndex()));

            }else if(holder instanceof  ViewHolderHeaderCalibration){

            }

        }

        @Override
        public int getItemCount() {
            return (calibrationItems.size() + 2);
        }

        private CalibrationItem getItem(int position) {
            return calibrationItems.get(position - 1);
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        private boolean isPositionFooter(int position) {
        return position == (getItemCount() - 1);
    }

        @Override
        public int getItemViewType(int position) {

            if (isPositionHeader(position))
                return TYPE_HEADER;
            else if(isPositionFooter(position))
                return TYPE_FOOTER;

            return TYPE_ITEM;
        }

        public ArrayList<RecyclerView.ViewHolder> getAllBoundViewHolders(){
            return boundViewHolders;
        }

        /*@Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            boundViewHolders.remove(holder);
        }*/


}

