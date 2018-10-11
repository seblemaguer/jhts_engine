#ifndef HELPERS_C
#define HELPERS_C

#ifdef __cplusplus
#define HELPERS_C_START extern "C" {
#define HELPERS_C_END   }
#else
#define HELPERS_C_START
#define HELPERS_C_END
#endif                          /* __CPLUSPLUS */

HELPERS_C_START;

#include "../../../hts_engine/lib/HTS_hidden.h"

/* Helper to provide just the parameter generation structure without the vocoder part */
HTS_Boolean HTS_MinimalGStreamSet_create(HTS_GStreamSet * gss, HTS_PStreamSet * pss, size_t fperiod) {
   size_t i, j, k;
   size_t msd_frame;

   /* initialize */
   gss->nstream = HTS_PStreamSet_get_nstream(pss);
   gss->total_frame = HTS_PStreamSet_get_total_frame(pss);
   gss->total_nsample = fperiod * gss->total_frame;
   gss->gstream = (HTS_GStream *) HTS_calloc(gss->nstream, sizeof(HTS_GStream));
   for (i = 0; i < gss->nstream; i++) {
      gss->gstream[i].vector_length = HTS_PStreamSet_get_vector_length(pss, i);
      gss->gstream[i].par = (double **) HTS_calloc(gss->total_frame, sizeof(double *));
      for (j = 0; j < gss->total_frame; j++)
         gss->gstream[i].par[j] = (double *) HTS_calloc(gss->gstream[i].vector_length, sizeof(double));
   }

   /* copy generated parameter */
   for (i = 0; i < gss->nstream; i++) {
      if (HTS_PStreamSet_is_msd(pss, i)) {      /* for MSD */
         for (j = 0, msd_frame = 0; j < gss->total_frame; j++)
            if (HTS_PStreamSet_get_msd_flag(pss, i, j) == TRUE) {
               for (k = 0; k < gss->gstream[i].vector_length; k++)
                  gss->gstream[i].par[j][k] = HTS_PStreamSet_get_parameter(pss, i, msd_frame, k);
               msd_frame++;
            } else
               for (k = 0; k < gss->gstream[i].vector_length; k++)
                  gss->gstream[i].par[j][k] = HTS_NODATA;
      } else {                  /* for non MSD */
         for (j = 0; j < gss->total_frame; j++)
            for (k = 0; k < gss->gstream[i].vector_length; k++)
               gss->gstream[i].par[j][k] = HTS_PStreamSet_get_parameter(pss, i, j, k);
      }
   }

   return TRUE;
}


#endif                          /* !HELPERS_C */
