package com.npairlines.ui.seat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.npairlines.data.model.Seat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatMapView extends View {
    private List<Seat> seats = new ArrayList<>();
    private final Paint paintAvailable = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBooked = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLocked = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBusiness = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private float seatSize = 0f;
    private float gap = 0f;
    private float aisleWidth = 0f;
    private float leftMargin = 0f;
    private int cols = 4; // 2-2 configuration
    
    private OnSeatClickListener listener;
    private Set<String> selectedSeatIds = new HashSet<>();
    private int maxSeats = 1; // Maximum seats that can be selected

    public SeatMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintAvailable.setColor(0xFF4CAF50);
        paintBooked.setColor(0xFFBDBDBD);
        paintLocked.setColor(0xFFFF9800);
        paintSelected.setColor(0xFF2196F3);
        paintBusiness.setColor(0xFFFFD700);
        
        strokePaint.setColor(0xFF1565C0);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(6f);
        
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        headerPaint.setColor(0xFF616161);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
        requestLayout();
        invalidate();
    }
    
    public void setMaxSeats(int max) {
        this.maxSeats = max;
    }
    
    public Set<String> getSelectedSeatIds() {
        return new HashSet<>(selectedSeatIds);
    }
    
    public List<Seat> getSelectedSeats() {
        List<Seat> selected = new ArrayList<>();
        for (Seat seat : seats) {
            if (selectedSeatIds.contains(seat.getId())) {
                selected.add(seat);
            }
        }
        return selected;
    }

    public void setListener(OnSeatClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        
        leftMargin = width * 0.08f;
        float usableWidth = width - leftMargin * 2;
        aisleWidth = usableWidth * 0.12f;
        float seatsWidth = usableWidth - aisleWidth;
        gap = seatsWidth * 0.04f;
        seatSize = (seatsWidth - 3 * gap) / 4;
        
        textPaint.setTextSize(seatSize * 0.4f);
        headerPaint.setTextSize(seatSize * 0.35f);
        
        // Calculate height with extra padding for legend
        int rowCount = (seats.size() + cols - 1) / cols;
        float headerHeight = seatSize * 0.8f;
        float legendHeight = seatSize * 2.0f; // Increased space for legend
        int height = (int) (headerHeight + (rowCount * (seatSize + gap)) + legendHeight + gap * 4); // Extra bottom padding
        
        setMeasuredDimension(width, Math.max(height, 500));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (seats.isEmpty()) return;
        
        float headerY = seatSize * 0.5f;
        
        float colAX = leftMargin + seatSize / 2;
        float colBX = leftMargin + seatSize + gap + seatSize / 2;
        float colCX = leftMargin + 2 * seatSize + 2 * gap + aisleWidth + seatSize / 2;
        float colDX = leftMargin + 3 * seatSize + 3 * gap + aisleWidth + seatSize / 2;
        
        canvas.drawText("A", colAX, headerY, headerPaint);
        canvas.drawText("B", colBX, headerY, headerPaint);
        canvas.drawText("C", colCX, headerY, headerPaint);
        canvas.drawText("D", colDX, headerY, headerPaint);
        
        float startY = seatSize * 0.8f;
        int colIndex = 0;
        int rowIndex = 0;

        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            
            float left, top;
            top = startY + rowIndex * (seatSize + gap);
            
            if (colIndex == 0) {
                left = leftMargin;
            } else if (colIndex == 1) {
                left = leftMargin + seatSize + gap;
            } else if (colIndex == 2) {
                left = leftMargin + 2 * seatSize + 2 * gap + aisleWidth;
            } else {
                left = leftMargin + 3 * seatSize + 3 * gap + aisleWidth;
            }
            
            float right = left + seatSize;
            float bottom = top + seatSize;
            RectF rect = new RectF(left, top, right, bottom);
            
            if (colIndex == 0) {
                headerPaint.setTextSize(seatSize * 0.3f);
                canvas.drawText(String.valueOf(rowIndex + 1), leftMargin / 2, rect.centerY() + headerPaint.getTextSize() / 3, headerPaint);
                headerPaint.setTextSize(seatSize * 0.35f);
            }
            
            Paint paint;
            boolean isSelected = selectedSeatIds.contains(seat.getId());
            
            if (isSelected) {
                paint = paintSelected;
            } else if ("BOOKED".equals(seat.getStatus())) {
                paint = paintBooked;
            } else if ("LOCKED".equals(seat.getStatus())) {
                paint = paintLocked;
            } else if ("BUSINESS".equals(seat.getSeatClass())) {
                paint = paintBusiness;
            } else {
                paint = paintAvailable;
            }
            
            float corner = seatSize * 0.15f;
            canvas.drawRoundRect(rect, corner, corner, paint);
            
            if (isSelected) {
                canvas.drawRoundRect(rect, corner, corner, strokePaint);
            }
            
            canvas.drawText(seat.getSeatNumber(), rect.centerX(), rect.centerY() + textPaint.getTextSize() / 3, textPaint);
            
            colIndex++;
            if (colIndex >= cols) {
                colIndex = 0;
                rowIndex++;
            }
        }
        
        // Legend - positioned closer but with plenty of space below
        float legendY = startY + (rowIndex) * (seatSize + gap) + gap * 2; // Closer to seats
        float legendSize = seatSize * 0.32f;
        float legendTextSize = legendSize * 0.85f;
        headerPaint.setTextSize(legendTextSize);
        headerPaint.setTextAlign(Paint.Align.LEFT);
        
        float legendSpacing = getWidth() / 2.2f;
        
        float x1 = leftMargin;
        canvas.drawRoundRect(x1, legendY, x1 + legendSize, legendY + legendSize, 4, 4, paintAvailable);
        canvas.drawText("Available", x1 + legendSize + 6, legendY + legendSize * 0.8f, headerPaint);
        
        float x2 = legendSpacing;
        canvas.drawRoundRect(x2, legendY, x2 + legendSize, legendY + legendSize, 4, 4, paintBooked);
        canvas.drawText("Booked", x2 + legendSize + 6, legendY + legendSize * 0.8f, headerPaint);
        
        float legendY2 = legendY + legendSize + gap;
        canvas.drawRoundRect(x1, legendY2, x1 + legendSize, legendY2 + legendSize, 4, 4, paintSelected);
        canvas.drawText("Selected", x1 + legendSize + 6, legendY2 + legendSize * 0.8f, headerPaint);
        
        canvas.drawRoundRect(x2, legendY2, x2 + legendSize, legendY2 + legendSize, 4, 4, paintBusiness);
        canvas.drawText("Business", x2 + legendSize + 6, legendY2 + legendSize * 0.8f, headerPaint);
        
        headerPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        
        float touchX = event.getX();
        float touchY = event.getY();
        float startY = seatSize * 0.8f;
        
        int colIndex = 0;
        int rowIndex = 0;
        
        for (Seat seat : seats) {
            float left, top;
            top = startY + rowIndex * (seatSize + gap);
            
            if (colIndex == 0) {
                left = leftMargin;
            } else if (colIndex == 1) {
                left = leftMargin + seatSize + gap;
            } else if (colIndex == 2) {
                left = leftMargin + 2 * seatSize + 2 * gap + aisleWidth;
            } else {
                left = leftMargin + 3 * seatSize + 3 * gap + aisleWidth;
            }
            
            float right = left + seatSize;
            float bottom = top + seatSize;
            
            if (touchX >= left && touchX <= right && touchY >= top && touchY <= bottom) {
                boolean isAvailable = "AVAILABLE".equals(seat.getStatus()) || 
                    ("BUSINESS".equals(seat.getSeatClass()) && !"BOOKED".equals(seat.getStatus()) && !"LOCKED".equals(seat.getStatus()));
                
                if (isAvailable) {
                    // Toggle selection
                    if (selectedSeatIds.contains(seat.getId())) {
                        // Deselect
                        selectedSeatIds.remove(seat.getId());
                    } else {
                        // Select if under max
                        if (selectedSeatIds.size() < maxSeats) {
                            selectedSeatIds.add(seat.getId());
                        } else if (maxSeats == 1) {
                            // Single selection mode - replace
                            selectedSeatIds.clear();
                            selectedSeatIds.add(seat.getId());
                        }
                        // If at max and maxSeats > 1, do nothing
                    }
                    invalidate();
                }
                if (listener != null) {
                    listener.onSeatClick(seat, selectedSeatIds.size());
                }
                return true;
            }
            
            colIndex++;
            if (colIndex >= cols) {
                colIndex = 0;
                rowIndex++;
            }
        }
        return true;
    }
    
    public interface OnSeatClickListener {
        void onSeatClick(Seat seat, int totalSelected);
    }
}
