package com.example.marketplace;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        switch (position){
            case 0:
                return new InicioFragment();
            case 1:
                return new MisProductosFragment();
            case 2:
                return new PerfilFragment();
            case 3:
                return new AgregarProductosFragment();
            default:
                return new InicioFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
