package com.xinrui.smart.pojo;

import com.xinrui.smart.R;

import java.util.ArrayList;



/**
 * Depiction:
 * Author: teach
 * Date: 2017/3/20 15:51
 */
public class GroupModel {

    /**
     * 获取组列表数据
     *
     * @param groupCount    组数量
     * @param childrenCount 每个组里的子项数量
     * @return
     */
    public static boolean isOpenAll=false;
    private static int[] imgs={R.drawable.switch_close,R.drawable.switch_open};
    private static int[]colors={R.color.color_white,R.color.color_orange};
    public static ArrayList<GroupEntry> getGroups(int groupCount, int childrenCount) {
        ArrayList<GroupEntry> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            ArrayList<ChildEntry> children = new ArrayList<>();
            for (int j = 0; j < childrenCount; j++) {
                if (isOpenAll){
                    children.add(new ChildEntry("第" + (i + 1) + "组第" + (j + 1) + "项",imgs[1]));
                }else{
                    children.add(new ChildEntry("第" + (i + 1) + "组第" + (j + 1) + "项",imgs[0]));
                }

            }
            groups.add(new GroupEntry("第" + (i + 1) + "组头部",colors[0], children));

        }
        return groups;
    }

    /**
     * 获取可展开收起的组列表数据(默认展开)
     *
     * @param groupCount    组数量
     * @param childrenCount 每个组里的子项数量
     * @return
     */
    public static ArrayList<ExpandableGroupEntity> getExpandableGroups(int groupCount, int childrenCount) {
        ArrayList<ExpandableGroupEntity> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            ArrayList<ChildEntry> children = new ArrayList<>();
            for (int j = 0; j < childrenCount; j++) {
                children.add(new ChildEntry("第" + (i + 1) + "组第" + (j + 1) + "项"));
            }
            groups.add(new ExpandableGroupEntity("第" + (i + 1) + "组头部",
                    "第" + (i + 1) + "组尾部", true, children));
        }
        return groups;
    }

}
