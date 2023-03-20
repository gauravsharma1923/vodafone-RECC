/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recc;

/**
 *
 * @author ch-e00246
 */
public class SingleToneAcess {

    private boolean DelhiFlag = true;
    private boolean MumbaiFlag = true;
    private boolean KolkataFlag = true;
    private boolean ChennaiFlag = true;
    private String strDelhiError="";
    private String strMumbaiError="";
    private String strKolkataError="";
    private String strChennaiError="";

    static SingleToneAcess objSingleToneAcess = null;
    
    private SingleToneAcess() {
    }

    public synchronized static SingleToneAcess getObject() {
        if (objSingleToneAcess == null) {
            System.out.println("************Creating New Object of This class");
            objSingleToneAcess = new SingleToneAcess();
        }
        return objSingleToneAcess;

    }

    public boolean isChennaiFlag() {
        return ChennaiFlag;
    }

    public void setChennaiFlag(boolean ChennaiFlag) {
        this.ChennaiFlag = ChennaiFlag;
        UpdateDb("Chennai");
    }

    public boolean isDelhiFlag() {
        return DelhiFlag;
    }

    public void setDelhiFlag(boolean DelhiFlag) {
        this.DelhiFlag = DelhiFlag;
        UpdateDb("Delhi");
    }

    public boolean isKolkataFlag() {
        return KolkataFlag;
    }

    public void setKolkataFlag(boolean KolkataFlag) {
        this.KolkataFlag = KolkataFlag;
        UpdateDb("Kolkata");
    }

    public boolean isMumbaiFlag() {
        return MumbaiFlag;
    }

    public void setMumbaiFlag(boolean MumbaiFlag) {
        this.MumbaiFlag = MumbaiFlag;
        UpdateDb("Mumbai");
    }

   public void setDelhiError(String strDelhiError)
   {
       this.strDelhiError=strDelhiError;
   }
   public String getDelhiError()
   {
      return strDelhiError;
   }

   public void setMumbaiError(String strMumbaiError)
   {
       this.strMumbaiError=strMumbaiError;
   }
   public String getMumbaiError()
   {
      return strMumbaiError;
   }

   public void setKolkataError(String strKolkataError)
   {
       this.strKolkataError=strKolkataError;
   }
   public String getKolkataError()
   {
      return strKolkataError;
   }

   public void setChennaiError(String strChennaiError)
   {
       this.strChennaiError=strChennaiError;
   }
   public String getChennaiError()
   {
      return strChennaiError;
   }

   

    private void UpdateDb(String strCircle) {
        DbOperation objDbOperation = new DbOperation();
        objDbOperation.upFilureHitCircleWise(strCircle);

    }
    public void setDelhiFlagByJsp(boolean DelhiFlag) {
        this.DelhiFlag = DelhiFlag;

    }
    public void setMumbaiFlagByJsp(boolean MumbaiFlag) {
        this.MumbaiFlag = MumbaiFlag;

    }
    public void setChennaiFlagByJsp(boolean ChennaiFlag) {
        this.ChennaiFlag = ChennaiFlag;
       
    }
    public void setKolkataFlagByJsp(boolean KolkataFlag) {
        this.KolkataFlag = KolkataFlag;

    }

}
