package com.example.mark1;

public class Record
{
    String amount;
    String apartmentCode;
    String name;
    boolean status;


    public Record()
    {

    }

    public Record(String amount, String apartmentCode, String name, boolean status)
    {
        this.amount = amount;
        this.name = name;
        this.status = status;
        this.apartmentCode = apartmentCode;
    }

    public String getAmount()
    {
        return amount;
    }

    public void setAmount(String amount)
    {
        this.amount = amount;
    }

    public String getApartmentCode()
    {
        return apartmentCode;
    }

    public void setApartmentCode(String apartmentCode)
    {
        this.apartmentCode = apartmentCode;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isStatus()
    {
        return status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }
}
